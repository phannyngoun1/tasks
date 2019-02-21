package com.dream.workflow.usecase.port

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.dream.workflow.usecase.AccountAggregateUseCase.Protocol._

trait AccountAggregateFlows {



  def create:  Flow[CreateAccountCmdReq, CreateAccountCmdRes, NotUsed]

  def get: Flow[GetAccountCmdReq, GetAccountCmdRes, NotUsed]

  def assignParticipant: Flow[AssignParticipantCmdReq, AssignParticipantCmdRes, NotUsed]
}
