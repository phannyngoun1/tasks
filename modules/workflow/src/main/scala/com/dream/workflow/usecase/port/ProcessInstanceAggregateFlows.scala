package com.dream.workflow.usecase.port

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.dream.workflow.entity.processinstance.ProcessInstanceProtocol._
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol
import com.dream.workflow.usecase.ProcessInstanceAggregateUseCase.Protocol.{CreatePInstCmdResponse, GetPInstCmdResponse}

trait ProcessInstanceAggregateFlows {

  def createInst: Flow[CreatePInstCmdRequest, CreatePInstCmdResponse, NotUsed]

  def getPInst: Flow[Protocol.GetPInstCmdRequest, GetPInstCmdResponse, NotUsed]

  def performTask: Flow[PerformTaskCmdReq, PerformTaskCmdRes, NotUsed]

}
