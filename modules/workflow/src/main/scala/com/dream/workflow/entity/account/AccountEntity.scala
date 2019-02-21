package com.dream.workflow.entity.account

import java.util.UUID

import akka.actor.{ActorLogging, Props}
import akka.persistence._
import cats.implicits._
import com.dream.common.EntityState
import com.dream.common.Protocol.CmdResponseFailed
import com.dream.common.domain.ResponseError
import com.dream.workflow.domain.Account._
import com.dream.workflow.domain.Account
import com.dream.workflow.entity.account.AccountProtocol._

object AccountEntity {

  final val AggregateName = "acc"

  def prop = Props(new AccountEntity)

  def name(uuId: UUID): String = uuId.toString

  implicit class EitherOps(val self: Either[AccountError, Account]) {
    def toSomeOrThrow: Option[Account] = self.fold(error => throw new IllegalStateException(error.message), Some(_))
  }
}

class AccountEntity extends PersistentActor with ActorLogging with EntityState[AccountError,Account]{

  import AccountEntity._

  var state: Option[Account] = None

  val numOfEventsToSnapshot = 3

  private def applyState(event: AccountCreated): Either[AccountError, Account] =
    Either.right(
      Account(
        event.id,
        event.name,
        event.fullName,
        event.currentParticipantId
      )
    )

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, _state: Account) =>
      println(s"SnapshotOffer ${_state}")
      state = Some(_state)
    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"receiveRecover: SaveSnapshotSuccess succeeded: $metadata")
    case SaveSnapshotFailure(metadata, reason) ⇒
      log.debug(s"SaveSnapshotFailure: SaveSnapshotSuccess failed: $metadata, ${reason}")
    case event: AccountCreated=>
      println(s"replay event: $event")
      state = applyState(event).toSomeOrThrow
    case event: ParticipantAssigned =>
      println(s"replay event: $event")
      state = mapState(_.assignParticipant(event.participantId)).toSomeOrThrow
    case RecoveryCompleted =>
      println(s"Recovery completed: $persistenceId")
    case _ => log.debug("Other")

  }

  override def receiveCommand: Receive = {

    case cmd: CreateAccountCmdRequest => persist(AccountCreated(cmd.id, cmd.name, cmd.fullName, cmd.participantId)) { event =>
      sender() ! CreateAccountCmdSuccess(cmd.id)
      state = applyState(event).toSomeOrThrow
    }

    case GetAccountCmdRequest(id) if equalsId(id)(state, _.id.equals(id)) =>
      foreachState{ state =>

        println(s"retrive: ${state}")

        sender() ! GetAccountCmdSuccess(state)
      }

    case AssignParticipantCmdRequest(id, participantId)  if equalsId(id)(state, _.id.equals(id)) =>
      mapState(_.assignParticipant(participantId)) match {
        case Left(error) => sender() ! CmdResponseFailed( ResponseError(Some(id), error.message))
        case Right(newState) => persist(ParticipantAssigned(id, participantId)) { event =>
          state = Some(newState)
          sender() ! AssignParticipantCmdSuccess(id)
          tryToSaveSnapshot()
        }
    }

    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"receiveCommand: SaveSnapshotSuccess succeeded: $metadata")
    case cmd: AccountCmdRequest =>  println(s"no handler for : ${cmd} ")
  }

  override def persistenceId: String =  s"$AggregateName-${self.path.name}"

  override protected def foreachState(f: Account => Unit): Unit =
      Either.fromOption(state, InvalidAccountStateError()).filterOrElse(_.isActive, InvalidAccountStateError()).foreach(f)

  override protected def mapState(f: Account => Either[AccountError, Account]): Either[AccountError, Account] =
    for {
      state    <- Either.fromOption(state, InvalidAccountStateError())
      newState <- f(state)
    } yield newState



  private def tryToSaveSnapshot(): Unit =
    if (lastSequenceNr % numOfEventsToSnapshot == 0) {
      foreachState(saveSnapshot)
    }

  override protected def invaliStateError(id: Option[UUID]): AccountError =
    InvalidAccountStateError(id)
}
