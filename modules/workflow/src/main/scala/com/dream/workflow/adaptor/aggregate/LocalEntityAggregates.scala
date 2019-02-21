package com.dream.workflow.adaptor.aggregate

import akka.actor.{Actor, Props}


object LocalEntityAggregates {

  def props = Props(new LocalEntityAggregates)

def name = "local-process-instances"
}

class LocalEntityAggregates  extends Actor  with AggregatesLookup {
  override def receive: Receive = forwardToEntityAggregate
}
