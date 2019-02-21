package com.dream.workflow.usecase

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import com.dream.common.UseCaseSupport
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.{Params, ParticipantAccess, StartAction, StartActivity, Flow => WFlow}
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol.{PerformTaskCmdReq, CreatePInstCmdRequest => CreateInst}
import com.dream.workflow.usecase.ItemAggregateUseCase.Protocol.{GetItemCmdRequest, GetItemCmdSuccess}
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.TaskToDest
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol.{GetWorkflowCmdRequest, GetWorkflowCmdSuccess}
import com.dream.workflow.usecase.port.{ItemAggregateFlows, ParticipantAggregateFlows, ProcessInstanceAggregateFlows, WorkflowAggregateFlows}

import scala.concurrent.{ExecutionContext, Future, Promise}


object ProcessInstanceAggregateUseCase {

  case class TaskToDest(
    taskId: UUID,
    pInst: UUID,
    participantId: UUID
  )

  object Protocol {

    sealed trait ProcessInstanceCmdResponse

    sealed trait ProcessInstanceCmdRequest

    sealed trait CreateInstanceCmdResponse extends ProcessInstanceCmdResponse

    case class CreatePInstCmdRequest(
      itemID: UUID,
      by: UUID,
      params: Option[Params] = None
    ) extends ProcessInstanceCmdRequest

    sealed trait CreatePInstCmdResponse

    case class CreatePInstCmdSuccess(
      folio: String
    ) extends CreatePInstCmdResponse

    case class CreatePInstCmdFailed(error: ResponseError) extends CreatePInstCmdResponse

    case class GetPInstCmdRequest(id: UUID) extends ProcessInstanceCmdRequest

    sealed trait GetPInstCmdResponse extends ProcessInstanceCmdRequest

    case class GetPInstCmdSuccess(id: UUID, folio: String) extends GetPInstCmdResponse

    case class GetPInstCmdFailed(error: ResponseError) extends GetPInstCmdResponse

  }

}

class ProcessInstanceAggregateUseCase(
  processInstanceAggregateFlows: ProcessInstanceAggregateFlows,
  workflowAggregateFlows: WorkflowAggregateFlows,
  itemAggregateFlows: ItemAggregateFlows,
  participantAggregateFlows: ParticipantAggregateFlows
)(implicit system: ActorSystem)
  extends UseCaseSupport {

  import ProcessInstanceAggregateUseCase.Protocol._
  import UseCaseSupport._

  implicit val mat: Materializer = ActorMaterializer()




  private val prepareCreateInst = Flow.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val broadcast = b.add(Broadcast[CreatePInstCmdRequest](2))
    val createInstZip = b.add(Zip[WFlow, CreatePInstCmdRequest])
    val convertToGetItem = Flow[CreatePInstCmdRequest].map(r => GetItemCmdRequest(r.itemID))
    val convertToCreatePInstCmdReq = Flow[(WFlow, CreatePInstCmdRequest)].map(
      f => {
        val flow = f._1
        val req = f._2
        val startAction = StartAction()
        val startActivity = StartActivity()
        val nextFlow = flow.nextActivity(startAction, startActivity, ParticipantAccess(req.by), false) match {
          case Right(flow) => flow
        }

        CreateInst(
          id = UUID.randomUUID(),
          activityId = UUID.randomUUID(),
          flowId = flow.id,
          folio = "test",
          contentType = "ticket",
          activity = StartActivity(),
          action = StartAction(),
          by = req.by,
          description = "Test",
          destinations = nextFlow.participants,
          nextActivity =  nextFlow.activity,
          nextActions = nextFlow.actionFlows.map(_.action),
          todo = "todo"
        )
      }
    )

    broadcast.out(0) ~> convertToGetItem ~> itemAggregateFlows.getItem.map {
      case res: GetItemCmdSuccess => GetWorkflowCmdRequest(res.workflowId)
    } ~> workflowAggregateFlows.getWorkflow.map {
      case GetWorkflowCmdSuccess(workflow) => workflow
    } ~> createInstZip.in0

    broadcast.out(1) ~> createInstZip.in1

    val createPrepareB = b.add(Broadcast[CreateInst](3))
    val convertToTaskCmdRequestFlow = Flow[CreateInst].map(p => PerformTaskCmdReq(p.id, p.activityId))

    //TODO: adding real tasks

    val assignTaskCmdFlow = Flow[CreateInst].map(p => p.destinations.map(dest => TaskToDest(UUID.randomUUID(), p.id, dest)))


    val out = createInstZip.out ~> convertToCreatePInstCmdReq ~> createPrepareB ~> processInstanceAggregateFlows.createInst
    createPrepareB ~> convertToTaskCmdRequestFlow ~> processInstanceAggregateFlows.performTask ~> Sink.ignore
    createPrepareB ~> assignTaskCmdFlow ~> Sink.foreach(println)

    FlowShape(broadcast.in, out.outlet)
  })


  private val createInstanceFlow
  : SourceQueueWithComplete[(CreatePInstCmdRequest, Promise[CreatePInstCmdResponse])] = Source
    .queue[(CreatePInstCmdRequest, Promise[CreatePInstCmdResponse])](10, OverflowStrategy.dropNew)
    .via(prepareCreateInst.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  private val getPInstFlow: SourceQueueWithComplete[(GetPInstCmdRequest, Promise[GetPInstCmdResponse])] = Source
    .queue[(GetPInstCmdRequest, Promise[GetPInstCmdResponse])](10, OverflowStrategy.dropNew)
    .via(processInstanceAggregateFlows.getPInst.zipPromise)
    .toMat(completePromiseSink)(Keep.left)
    .run()

  def createPInst(request: CreatePInstCmdRequest)(implicit ec: ExecutionContext): Future[CreatePInstCmdResponse] = {
    offerToQueue(createInstanceFlow)(request, Promise())
  }

  def getPInst(request: GetPInstCmdRequest)(implicit ec: ExecutionContext): Future[GetPInstCmdResponse] =
    offerToQueue(getPInstFlow)(request, Promise())

}
