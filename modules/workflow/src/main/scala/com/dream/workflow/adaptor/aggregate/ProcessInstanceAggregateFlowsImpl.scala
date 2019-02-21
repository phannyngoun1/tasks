package com.dream.workflow.adaptor.aggregate

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.dream.common.Protocol.CmdResponseFailed
import com.dream.common.domain.ResponseError
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol.{PerformTaskCmdRes, _}
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol
import com.dream.workflow.usecase.port.ProcessInstanceAggregateFlows

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class ProcessInstanceAggregateFlowsImpl(aggregateRef: ActorRef) extends ProcessInstanceAggregateFlows {

  private implicit val to: Timeout = Timeout(2 seconds)

  override def createInst: Flow[CreatePInstCmdRequest, Protocol.CreatePInstCmdResponse, NotUsed] =
    Flow[CreatePInstCmdRequest]
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: CreatePInstCmdSuccess => Protocol.CreatePInstCmdSuccess(res.id.toString)
        case  CmdResponseFailed(message) => Protocol.CreatePInstCmdFailed(ResponseError(message))
      }

  override def performTask: Flow[PerformTaskCmdReq, PerformTaskCmdRes, NotUsed] =
    Flow[PerformTaskCmdReq].mapAsync(1) {
      case _ =>
        println("performTask")
        Future.successful(PerformTaskSuccess())
    }

  override def getPInst: Flow[Protocol.GetPInstCmdRequest, Protocol.GetPInstCmdResponse, NotUsed] =
    Flow[Protocol.GetPInstCmdRequest]
    .map(req => GetPInstCmdRequest(req.id))
    .mapAsync(1)(aggregateRef ? _)
    .map {
      case GetPInstCmdSuccess(pInst) => Protocol.GetPInstCmdSuccess(pInst.id, pInst.folio)
      case  CmdResponseFailed(message) => Protocol.GetPInstCmdFailed(ResponseError(message))
    }
}
