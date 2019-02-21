package com.dream.workflow.usecase

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy}
import com.dream.common.UseCaseSupport
import com.dream.common.domain.ResponseError
import com.dream.workflow.usecase.port.AccountAggregateFlows

import scala.concurrent.{ExecutionContext, Future, Promise}

object AccountAggregateUseCase {

  object Protocol {

    sealed trait AccountCmdResponse

    sealed trait AccountCmdRequest

    case class CreateAccountCmdReq(
      id: UUID,
      name: String,
      fullName: String,
      participantId: Option[UUID] = None
    ) extends AccountCmdRequest

    sealed trait CreateAccountCmdRes extends AccountCmdResponse

    case class CreateAccountCmdSuccess(
      id: UUID
    ) extends CreateAccountCmdRes


    case class CreateAccountCmdFailed(
      error: ResponseError
    ) extends CreateAccountCmdRes

    case class GetAccountCmdReq(id: UUID) extends AccountCmdRequest

    sealed trait GetAccountCmdRes extends AccountCmdResponse

    case class GetAccountCmdSuccess(
      id: UUID,
      name: String,
      fullName: String,
      curParticipantId: Option[UUID] = None
    ) extends GetAccountCmdRes

    case class GetAccountCmdFailed(responseError: ResponseError) extends GetAccountCmdRes

    case class AssignParticipantCmdReq(id: UUID, participantId: UUID) extends AccountCmdRequest

    sealed trait AssignParticipantCmdRes extends AccountCmdResponse

    case class AssignParticipantCmdSuccess(id: UUID) extends AssignParticipantCmdRes

    case class AssignParticipantCmdFailed(responseError: ResponseError) extends AssignParticipantCmdRes

  }

}

class AccountAggregateUseCase(flow: AccountAggregateFlows)(implicit system: ActorSystem) extends UseCaseSupport {

  import AccountAggregateUseCase.Protocol._
  import UseCaseSupport._

  implicit val mat: Materializer = ActorMaterializer()

  private val bufferSize: Int = 10

  private val createAccountQueue: SourceQueueWithComplete[(CreateAccountCmdReq, Promise[CreateAccountCmdRes])] =
    Source.queue[(CreateAccountCmdReq, Promise[CreateAccountCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(flow.create.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  private val getAccountQueue: SourceQueueWithComplete[(GetAccountCmdReq, Promise[GetAccountCmdRes])] =
    Source.queue[(GetAccountCmdReq, Promise[GetAccountCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(flow.get.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  private val assignParticipantQueue: SourceQueueWithComplete[(AssignParticipantCmdReq, Promise[AssignParticipantCmdRes])] =
    Source.queue[(AssignParticipantCmdReq, Promise[AssignParticipantCmdRes])](bufferSize, OverflowStrategy.dropNew)
      .via(flow.assignParticipant.zipPromise)
      .toMat(completePromiseSink)(Keep.left)
      .run()

  def createAccount(req: CreateAccountCmdReq)(implicit ec: ExecutionContext): Future[CreateAccountCmdRes] =
    offerToQueue(createAccountQueue)(req, Promise())

  def getAccount(req: GetAccountCmdReq)(implicit ec: ExecutionContext): Future[GetAccountCmdRes] =
    offerToQueue(getAccountQueue)(req, Promise())

  def assignParticipant(req: AssignParticipantCmdReq)(implicit ec: ExecutionContext): Future[AssignParticipantCmdRes] =
    offerToQueue(assignParticipantQueue)(req, Promise())


}
