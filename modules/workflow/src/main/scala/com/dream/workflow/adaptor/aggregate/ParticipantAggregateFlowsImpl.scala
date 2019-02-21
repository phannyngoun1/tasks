package com.dream.workflow.adaptor.aggregate

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.ask
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import com.dream.common.Protocol.CmdResponseFailed
import com.dream.common.domain.ResponseError
import com.dream.workflow.entity.participant.ParticipantProtocol._
import com.dream.workflow.usecase.ParticipantAggregateUseCase.Protocol
import com.dream.workflow.usecase.port.ParticipantAggregateFlows

import scala.concurrent.duration._
import scala.language.postfixOps

class ParticipantAggregateFlowsImpl(aggregateRef: ActorRef) extends ParticipantAggregateFlows {

  private implicit val to: Timeout = Timeout(2 seconds)

  override def create: Flow[Protocol.CreateParticipantCmdReq, Protocol.CreateParticipantCmdRes, NotUsed] =
    Flow[Protocol.CreateParticipantCmdReq]
      .map(req => CreateParticipantCmdReq(
        id = req.id,
        accountId = req.accountId,
        teamId = req.teamId,
        departmentId = req.departmentId,
        propertyId = req.propertyId
      ))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case res: CreateParticipantCmdSuccess => Protocol.CreateParticipantCmdSuccess(res.id)
        case CmdResponseFailed(message) => Protocol.CreateParticipantCmdFailed(ResponseError(message))
      }

  override def get: Flow[Protocol.GetParticipantCmdReq, Protocol.GetParticipantCmdRes, NotUsed] =
    Flow[Protocol.GetParticipantCmdReq]
      .map(req => GetParticipantCmdReq(id = req.id))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case GetParticipantCmdSuccess(participant) => Protocol.GetParticipantCmdSuccess(participant)
        case CmdResponseFailed(message) => Protocol.GetParticipantCmdFailed(ResponseError(message))
      }


  override def assignTask: Flow[Protocol.AssignTaskCmdReq, Protocol.AssignTaskCmdRes, NotUsed] =
    Flow[Protocol.AssignTaskCmdReq]
      .map(req => AssignTaskCmdReq(
        id = req.id,
        taskId = req.taskId,
        pInstId = req.pInstId
      ))
      .mapAsync(1)(aggregateRef ? _)
      .map {
        case AssignTaskCmdSuccess(id) => Protocol.AssignTaskCmdSuccess(id)
        case CmdResponseFailed(message) => Protocol.AssignTaskCmdFailed(ResponseError(message))
      }

}

