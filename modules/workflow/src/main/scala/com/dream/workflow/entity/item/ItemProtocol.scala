package com.dream.workflow.entity.item

import java.util.UUID

import com.dream.common.Protocol.{CmdRequest, CmdResponse}
import com.dream.workflow.domain.Item.ItemError

object ItemProtocol {

  sealed trait ItemCmdRequest extends CmdRequest {
    val id: UUID
  }

  sealed trait ItemCmdResponse extends CmdResponse

  case class NewItemCmdRequest(
    val id: UUID,
    name: String,
    desc: String,
    workflowId: UUID

  ) extends ItemCmdRequest


  case class NewItemCmdSuccess(
    id: UUID
  ) extends ItemCmdResponse


  case class GetItemCmdRequest(
    id: UUID
  ) extends ItemCmdRequest

  case class GetItemCmdSuccess(
    id: UUID,
    name: String,
    desc: String,
    workflowId: UUID
  ) extends ItemCmdRequest


  case class GetWorkflowId(
    id: UUID
  ) extends ItemCmdRequest

  case class GetWorkflowCmdSuccess(
    workflowId: UUID
  ) extends ItemCmdResponse

}
