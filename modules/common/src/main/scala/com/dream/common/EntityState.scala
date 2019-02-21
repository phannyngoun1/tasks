package com.dream.common

import java.util.UUID

import akka.actor.Actor
import com.dream.common.Protocol.CmdResponseFailed
import com.dream.common.domain.ErrorMessage

trait EntityState[E <: ErrorMessage ,T] { this: Actor =>

  protected def foreachState(f: (T) => Unit): Unit

  protected def equalsId(id: UUID)(state: Option[T], f: (T) => Boolean): Boolean =
    state match {
      case None =>
        sender() !  CmdResponseFailed(invaliStateError(Some(id)))
        false
      case Some(state) =>
        f(state)
    }

  protected def mapState(
    f: (T) => Either[E, T]
  ): Either[E, T]


  protected def invaliStateError(id: Option[UUID]) : E
}
