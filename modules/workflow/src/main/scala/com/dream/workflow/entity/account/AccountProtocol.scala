package com.dream.workflow.entity.account

import java.util.UUID

import com.dream.common.Protocol.{CmdRequest, CmdResponse}
import com.dream.workflow.domain.Account
import com.dream.workflow.domain.Account.AccountError

object AccountProtocol {

  sealed trait AccountCmdRequest extends CmdRequest {
    val id: UUID
  }

  sealed trait AccountCmdResponse extends CmdResponse

  case class CreateAccountCmdRequest(
    id: UUID,
    name: String,
    fullName: String,
    participantId: Option[UUID] = None
  ) extends AccountCmdRequest

  case class CreateAccountCmdSuccess(
    id: UUID
  ) extends AccountCmdResponse

  case class CreateAccountCmdFailed(
    id: UUID,
    error: AccountError
  ) extends AccountCmdResponse

  case class GetAccountCmdRequest(
    id: UUID
  ) extends AccountCmdRequest

  case class GetAccountCmdSuccess(account: Account) extends AccountCmdResponse

  case class AssignParticipantCmdRequest(id: UUID, participantId: UUID) extends AccountCmdRequest

  case class AssignParticipantCmdSuccess(id: UUID) extends AccountCmdResponse

}
