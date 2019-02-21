package com.dream.workflow.adaptor.aggregate

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.dream.common.Protocol.CmdResponseFailed
import com.dream.common.domain.ResponseError
import com.dream.workflow.entity.item.ItemProtocol._
import com.dream.workflow.usecase.ItemAggregateUseCase.Protocol
import com.dream.workflow.usecase.port.ItemAggregateFlows

import scala.concurrent.duration._
import scala.language.postfixOps

class ItemAggregateFlowsImpl(aggregateRef: ActorRef) extends ItemAggregateFlows {

  private implicit val to: Timeout = Timeout(2 seconds)

  override def createItem: Flow[Protocol.CreateItemCmdRequest, Protocol.CreateItemCmdResponse, NotUsed] = {

    Flow[Protocol.CreateItemCmdRequest]
      .map(req => NewItemCmdRequest(req.id, req.name, req.desc, req.workflowId))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: NewItemCmdSuccess => Protocol.CreateItemCmdSuccess(res.id)
        case CmdResponseFailed(message) => Protocol.CreateItemCmdFailed(ResponseError(message))
      }
  }

  override def getItem: Flow[Protocol.GetItemCmdRequest, Protocol.GetItemCmdResponse, NotUsed] =
    Flow[Protocol.GetItemCmdRequest]
      .map(req => GetItemCmdRequest(req.id))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: GetItemCmdSuccess => Protocol.GetItemCmdSuccess(res.id, res.name, res.desc, res.workflowId)
        case CmdResponseFailed(message) => Protocol.GetItemCmdFailed(ResponseError(message))
      }
}
