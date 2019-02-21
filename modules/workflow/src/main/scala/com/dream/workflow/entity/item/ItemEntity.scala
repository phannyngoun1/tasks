package com.dream.workflow.entity.item

import java.util.UUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.persistence._
import cats.implicits._
import com.dream.common.EntityState
import com.dream.workflow.domain.Item._
import com.dream.workflow.domain._
import com.dream.workflow.entity.item.ItemProtocol._

object ItemEntity {

  def prop = Props(new ItemEntity)

  final val AggregateName = "item"

  def name(uuId: UUID): String = uuId.toString

  implicit class EitherOps(val self: Either[ItemError, Item]) {
    def toSomeOrThrow: Option[Item] = self.fold(error => throw new IllegalStateException(error.message), Some(_))
  }
}

class Test extends Actor with ActorLogging {
  override def receive: Receive = {
    case cmd: NewItemCmdRequest => {
      log.debug("item created")
      sender() ! NewItemCmdSuccess(cmd.id)
    }
  }
}

class ItemEntity extends PersistentActor with ActorLogging  with EntityState[ItemError,Item]{

  import ItemEntity._

  var state: Option[Item] = None

  private def applyState(event: ItemCreated): Either[ItemError, Item] =
    Either.right(
      Item(
        event.id,
        event.name,
        event.desc,
        event.workflowId
      )
    )

  protected def foreachState(f: (Item) => Unit): Unit =
    Either.fromOption(state, InvalidItemStateError()).filterOrElse(_.isActive, InvalidItemStateError()).foreach(f)

  protected def mapState(
    f: (Item) => Either[ItemError, Item]
  ): Either[ItemError, Item] =
    for {
      state    <- Either.fromOption(state, InvalidItemStateError())
      newState <- f(state)
    } yield newState

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, _state: Item) =>
      println(s"SnapshotOffer ${_state}")
      state = Some(_state)

    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"receiveRecover: SaveSnapshotSuccess succeeded: $metadata")
    case SaveSnapshotFailure(metadata, reason) ⇒
      log.debug(s"SaveSnapshotFailure: SaveSnapshotSuccess failed: $metadata, ${reason}")
    case event: ItemCreated =>
      println(s"replay event: $event")
      state = applyState(event).toSomeOrThrow
    case RecoveryCompleted =>
      println(s"Recovery completed: $persistenceId")
    case _ => log.debug("Other")

  }

  override def receiveCommand: Receive = {
    case cmd: NewItemCmdRequest =>
      persist(ItemCreated(cmd.id, cmd.name, cmd.desc, cmd.workflowId)) { event =>
        state = applyState(event).toSomeOrThrow
        sender() ! NewItemCmdSuccess(event.id)
      }
    case cmd: GetItemCmdRequest if equalsId(cmd.id)(state, _.id.equals(cmd.id)) =>
      foreachState{ state =>
        sender() ! GetItemCmdSuccess(state.id, state.name, state.desc, state.workflowId)
      }
    case SaveSnapshotSuccess(metadata) =>
      log.debug(s"receiveCommand: SaveSnapshotSuccess succeeded: $metadata")
  }

  override def persistenceId: String = s"$AggregateName-${self.path.name}"

  override protected def invaliStateError(id: Option[UUID]): ItemError =
    InvalidItemStateError(id)
}
