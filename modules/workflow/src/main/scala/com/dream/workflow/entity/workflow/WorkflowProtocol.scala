package com.dream.workflow.entity.workflow

import java.util.UUID

import com.dream.common.Protocol.{CmdRequest, CmdResponse}
import com.dream.workflow.domain.{BaseActivity, BaseActivityFlow, Flow}

object WorkflowProtocol {

  sealed trait WorkFlowCmdRequest extends CmdRequest {
    val id: UUID
  }

  sealed trait WorkFlowCmdResponse extends CmdResponse

  case class CreateWorkflowCmdRequest(
    id: UUID,
    initialActivity: BaseActivity,
    flowList: Seq[BaseActivityFlow]
  ) extends WorkFlowCmdRequest

  abstract class CreateWorkflowCmdResponse() extends WorkFlowCmdResponse

  case class CreateWorkflowCmdSuccess(id: UUID) extends CreateWorkflowCmdResponse

  case class GetWorkflowCmdRequest(
    id: UUID
  ) extends WorkFlowCmdRequest

  case class GetWorkflowCmdSuccess(
    workflow: Flow
  ) extends WorkFlowCmdResponse


}
