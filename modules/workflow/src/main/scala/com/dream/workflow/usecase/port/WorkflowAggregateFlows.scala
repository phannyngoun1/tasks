package com.dream.workflow.usecase.port

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.dream.workflow.usecase.WorkflowAggregateUseCase.Protocol._

trait WorkflowAggregateFlows {

  def createWorkflow: Flow[CreateWorkflowCmdRequest, CreateWorkflowCmdResponse, NotUsed]

  def getWorkflow:  Flow[GetWorkflowCmdRequest, GetWorkflowCmdResponse, NotUsed]


}
