package com.dream.workflow.usecase

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy}
import com.dream.common.UseCaseSupport
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.{BaseActivity, BaseActivityFlow, Flow}
import com.dream.workflow.usecase.port.WorkflowAggregateFlows

import scala.concurrent.{ExecutionContext, Future, Promise}


object WorkflowAggregateUseCase {

  object Protocol {

    sealed trait WorkflowCmdResponse

    sealed trait WorkflowCmdRequest

    case class CreateWorkflowCmdRequest(
      id: UUID,
      initialActivity: BaseActivity,
      workflowList: Seq[BaseActivityFlow],
    ) extends WorkflowCmdRequest

    abstract class CreateWorkflowCmdResponse() extends WorkflowCmdResponse

    case class CreateWorkflowCmdSuccess(id: UUID) extends CreateWorkflowCmdResponse

    case class CreateWorkflowCmdFailed(error: ResponseError) extends CreateWorkflowCmdResponse

    case class GetWorkflowCmdRequest(
      id: UUID
    ) extends WorkflowCmdRequest

    abstract class GetWorkflowCmdResponse() extends WorkflowCmdResponse

    case class GetWorkflowCmdSuccess(flow: Flow) extends GetWorkflowCmdResponse

    case class GetWorkflowCmdFailed(error: ResponseError) extends GetWorkflowCmdResponse

  }

}

class WorkflowAggregateUseCase(workflow: WorkflowAggregateFlows)(implicit system: ActorSystem) extends UseCaseSupport {

  import UseCaseSupport._
  import WorkflowAggregateUseCase.Protocol._

  implicit val mat: Materializer = ActorMaterializer()

  private val bufferSize: Int = 10

  def createWorkflow(request: CreateWorkflowCmdRequest)(implicit ec: ExecutionContext): Future[CreateWorkflowCmdResponse] = {
    offerToQueue(createWorkflowQueue)(request, Promise())
  }

  def getWorkflow(request: GetWorkflowCmdRequest)(implicit ec: ExecutionContext): Future[GetWorkflowCmdResponse] = {
    offerToQueue(getWorkflowQueue)(request, Promise())
  }

  private val createWorkflowQueue: SourceQueueWithComplete[(CreateWorkflowCmdRequest, Promise[CreateWorkflowCmdResponse])] =
    Source.queue[(CreateWorkflowCmdRequest, Promise[CreateWorkflowCmdResponse])](bufferSize, OverflowStrategy.dropNew)
      .via(workflow.createWorkflow.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  private val getWorkflowQueue: SourceQueueWithComplete[(GetWorkflowCmdRequest, Promise[GetWorkflowCmdResponse])] =
    Source.queue[(GetWorkflowCmdRequest, Promise[GetWorkflowCmdResponse])](bufferSize, OverflowStrategy.dropNew)
      .via(workflow.getWorkflow.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()
}
