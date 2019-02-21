package com.dream.workflow.domain

import java.time.Instant
import java.util.UUID

import com.dream.common.domain.ErrorMessage
import com.dream.workflow.domain.Account.{AccountError, AccountEvent, ParticipantAssigned}

object Account {

  sealed trait AccountEvent

  sealed trait AccountError extends ErrorMessage

  case class AccountCreated(
    id: UUID,
    name: String,
    fullName: String,
    currentParticipantId: Option[UUID] = None
  ) extends AccountEvent

  case class ParticipantAssigned(
    id: UUID,
    participantId: UUID
  ) extends AccountEvent

  case class InvalidAccountStateError(override val id: Option[UUID] = None) extends AccountError {
    override val message: String = s"Invalid state${id.fold("")(id => s":id = ${id.toString}")}"
  }

  case class ParticipantHis(
    currParticipantId: UUID,
    disassociateDate: Instant
  )

}

case class Account(
  id: UUID,
  name: String,
  fullName: String,
  currParticipantId: Option[UUID] = None,
  participantHist: List[UUID] = List.empty,
  isActive: Boolean = true
) {
  def assignParticipant(participantId: UUID): Either[AccountError, Account] =
    Right(copy(currParticipantId = Some(participantId)))

}

