package com.dream.workflow.adaptor.aggregate

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.dream.common.Protocol.CmdResponseFailed
import com.dream.common.domain.ResponseError
import com.dream.workflow.entity.workflow.WorkflowProtocol._
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol
import com.dream.workflow.usecase.port.WorkflowAggregateFlows

import scala.concurrent.duration._
import scala.language.postfixOps

class WorkflowAggregateFlowsImpl(aggregateRef: ActorRef) extends WorkflowAggregateFlows {

  private implicit val to: Timeout = Timeout(2 seconds)

  override def createWorkflow: Flow[Protocol.CreateWorkflowCmdRequest, Protocol.CreateWorkflowCmdResponse, NotUsed] =
    Flow[Protocol.CreateWorkflowCmdRequest]
      .map(req => CreateWorkflowCmdRequest(req.id, req.initialActivity, req.workflowList))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: CreateWorkflowCmdSuccess => Protocol.CreateWorkflowCmdSuccess(res.id)
        case CmdResponseFailed(message) => Protocol.CreateWorkflowCmdFailed(ResponseError(message))
      }

  override def getWorkflow: Flow[Protocol.GetWorkflowCmdRequest, Protocol.GetWorkflowCmdResponse, NotUsed] =
    Flow[Protocol.GetWorkflowCmdRequest]
      .map(req => GetWorkflowCmdRequest(req.id))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: GetWorkflowCmdSuccess => Protocol.GetWorkflowCmdSuccess(res.workflow)
        case CmdResponseFailed(message) => Protocol.GetWorkflowCmdFailed(ResponseError(message))
      }

}
