package com.dream.workflow.usecase.port

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.dream.workflow.usecase.ItemAggregateUseCase.Protocol._

trait ItemAggregateFlows {

  def createItem:  Flow[CreateItemCmdRequest, CreateItemCmdResponse, NotUsed]
  def getItem: Flow[GetItemCmdRequest, GetItemCmdResponse, NotUsed]

}
