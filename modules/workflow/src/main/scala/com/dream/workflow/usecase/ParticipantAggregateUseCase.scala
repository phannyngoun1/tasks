package com.dream.workflow.usecase

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Keep, Source, SourceQueueWithComplete}
import com.dream.common.UseCaseSupport
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.Participant
import com.dream.workflow.usecase.port.ParticipantAggregateFlows

import scala.concurrent.{ExecutionContext, Future, Promise}

object ParticipantAggregateUseCase {

  object Protocol {

    sealed trait ParticipantCmdResponse

    sealed trait ParticipantCmdRequest

    case class CreateParticipantCmdReq(
      id: UUID,
      accountId: UUID,
      teamId: UUID,
      departmentId: UUID,
      propertyId: UUID
    ) extends ParticipantCmdRequest

    sealed trait CreateParticipantCmdRes

    case class CreateParticipantCmdSuccess(
      id: UUID
    ) extends CreateParticipantCmdRes

    case class CreateParticipantCmdFailed(error: ResponseError) extends CreateParticipantCmdRes


    case class GetParticipantCmdReq(
      id: UUID
    ) extends ParticipantCmdRequest

    sealed trait GetParticipantCmdRes extends ParticipantCmdResponse

    case class GetParticipantCmdSuccess(
      participant: Participant
    ) extends GetParticipantCmdRes

    case class GetParticipantCmdFailed(error: ResponseError) extends GetParticipantCmdRes


    case class AssignTaskCmdReq(
      id: UUID,
      taskId: UUID,
      pInstId: UUID,
    ) extends ParticipantCmdRequest

    trait AssignTaskCmdRes extends ParticipantCmdResponse

    case class AssignTaskCmdSuccess(id: UUID) extends AssignTaskCmdRes

    case class AssignTaskCmdFailed(error: ResponseError) extends AssignTaskCmdRes

  }

}

class ParticipantAggregateUseCase(participantAggregateFlows: ParticipantAggregateFlows)(implicit system: ActorSystem) extends UseCaseSupport {

  import ParticipantAggregateUseCase.Protocol._
  import UseCaseSupport._

  //  implicit val mat: Materializer = ActorMaterializer()

  val decider: Supervision.Decider = {
    case _ => Supervision.Restart
  }

  implicit val mat = ActorMaterializer(
    ActorMaterializerSettings(system)
      .withSupervisionStrategy(decider)
  )

  private val bufferSize: Int = 10

  def createParticipant(req: CreateParticipantCmdReq)(implicit ec: ExecutionContext): Future[CreateParticipantCmdRes] =
    offerToQueue(createParticipantQueue)(req, Promise())

  def getParticipant(req: GetParticipantCmdReq)(implicit ec: ExecutionContext): Future[GetParticipantCmdRes] =
    offerToQueue(getParticipantQueue)(req, Promise())

  def assignTask(req: AssignTaskCmdReq)(implicit ec: ExecutionContext): Future[AssignTaskCmdRes] =
    offerToQueue(assignTaskQueue)(req, Promise())

  private val createParticipantQueue: SourceQueueWithComplete[(CreateParticipantCmdReq, Promise[CreateParticipantCmdRes])] =
    Source.queue[(CreateParticipantCmdReq, Promise[CreateParticipantCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(participantAggregateFlows.create.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  private val getParticipantQueue: SourceQueueWithComplete[(GetParticipantCmdReq, Promise[GetParticipantCmdRes])] =
    Source.queue[(GetParticipantCmdReq, Promise[GetParticipantCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(participantAggregateFlows.get.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()


  private val assignTaskQueue: SourceQueueWithComplete[(AssignTaskCmdReq, Promise[AssignTaskCmdRes])] =
    Source.queue[(AssignTaskCmdReq, Promise[AssignTaskCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(participantAggregateFlows.assignTask.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()
}
