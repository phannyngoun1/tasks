package com.dream.workflow.domain

import java.util.UUID


object FlowEvent {

  sealed trait FlowEvent {
    val id: UUID
  }

  case class FlowCreated(
    override val id: UUID,
    initialActivity: BaseActivity,
    flowList: Seq[BaseActivityFlow],
  ) extends FlowEvent


//  object FlowCreated {
//    implicit val format: Format[FlowCreated] = Json.format
//  }

}
