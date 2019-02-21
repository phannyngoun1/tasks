package com.dream.workflow.entity.workflow

import java.util.UUID

import akka.actor.{ActorLogging, Props}
import akka.persistence._
import cats.implicits._
import com.dream.common.EntityState
import com.dream.workflow.domain.FlowEvent.FlowCreated
import com.dream.workflow.domain._
import com.dream.workflow.entity.workflow.WorkflowProtocol._

object WorkflowEntity {

  def prop = Props(new WorkflowEntity)

  final val AggregateName = "work_flow"

  def name(uuId: UUID): String = uuId.toString

  implicit class EitherOps(val self: Either[WorkflowError, Flow]) {
    def toSomeOrThrow: Option[Flow] = self.fold(error => throw new IllegalStateException(error.message), Some(_))
  }

}

class WorkflowEntity extends PersistentActor with ActorLogging with EntityState[WorkflowError, Flow] {

  import WorkflowEntity._

  var state: Option[Flow] = None

  private def applyState(event: FlowCreated): Either[WorkflowError, Flow] =
    Either.right(
      Flow(
        event.id,
        event.initialActivity,
        event.flowList,
        true
      )
    )

  protected def foreachState(f: (Flow) => Unit): Unit =
    Either.fromOption(state, InvalidWorkflowStateError()).filterOrElse(_.isActive, InvalidWorkflowStateError()).foreach(f)

  override protected def mapState(f: Flow => Either[WorkflowError, Flow]): Either[WorkflowError, Flow] =
    for {
      state <- Either.fromOption(state, InvalidWorkflowStateError())
      newState <- f(state)
    } yield newState


  override def receiveRecover: Receive = {

    case SnapshotOffer(_, _state: Flow) =>
      state = Some(_state)
    case SaveSnapshotSuccess(metadata) =>
      log.info(s"receiveRecover: SaveSnapshotSuccess succeeded: $metadata")
    case SaveSnapshotFailure(metadata, reason) ⇒
      log.info(s"SaveSnapshotFailure: SaveSnapshotSuccess failed: $metadata, ${reason}")
    case event: FlowCreated =>
      state = applyState(event).toSomeOrThrow
    case RecoveryCompleted =>
      log.info(s"Recovery completed: $persistenceId")
    case _ => log.info("Other")
  }

  override def receiveCommand: Receive = {
    case cmd: CreateWorkflowCmdRequest => persist(FlowCreated(cmd.id, cmd.initialActivity, cmd.flowList)) { event =>
      state = applyState(event).toSomeOrThrow
      sender() ! CreateWorkflowCmdSuccess(event.id)
    }
    case cmd: GetWorkflowCmdRequest  => {
      foreachState { state =>
        sender() ! GetWorkflowCmdSuccess(state)
      }
    }
    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"receiveCommand: SaveSnapshotSuccess succeeded: $metadata")
  }

  override def persistenceId: String = s"$AggregateName-${self.path.name}"

  override protected def invaliStateError(id: Option[UUID]): WorkflowError =
    InvalidWorkflowStateError(id)
}
