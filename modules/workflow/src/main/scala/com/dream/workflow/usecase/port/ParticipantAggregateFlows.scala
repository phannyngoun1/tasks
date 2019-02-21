package com.dream.workflow.usecase.port

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.dream.workflow.usecase.ParticipantAggregateUseCase.Protocol

trait ParticipantAggregateFlows {

  def create:  Flow[Protocol.CreateParticipantCmdReq, Protocol.CreateParticipantCmdRes, NotUsed]
  def get: Flow[Protocol.GetParticipantCmdReq, Protocol.GetParticipantCmdRes, NotUsed]
  def assignTask: Flow[Protocol.AssignTaskCmdReq, Protocol.AssignTaskCmdRes, NotUsed]

}
