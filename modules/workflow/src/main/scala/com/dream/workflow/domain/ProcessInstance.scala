package com.dream.workflow.domain

import java.util.UUID

import com.dream.common.domain.ErrorMessage
import com.dream.workflow.domain.ProcessInstance.Task


object ProcessInstance {

  sealed trait InstError extends ErrorMessage

  case class DefaultInstError(message: String) extends InstError

  case class InvalidInstStateError(override val id: Option[UUID] = None) extends InstError {

    override val message: String = s"Invalid state${id.fold("")(id => s":id = ${id.toString}")}"
  }


  case class Task(
    description: String,
    participants: List[UUID],
    //Priority
    activity: BaseActivity
  )

  case class InstanceType(
    id: UUID,
    name: String,
    description: String
  )


  sealed trait ProcessInstanceEvent

  case class ProcessInstanceCreated(

    id: UUID,
    activityId: UUID,
    flowId: UUID,
    folio: String,
    contentType: String,
    activity: BaseActivity,
    action: BaseAction,
    by: UUID,
    description: String,
    destinations: List[UUID],
    nextActivity: BaseActivity,
    todo: String
  ) extends ProcessInstanceEvent

}

case class ProcessInstance(
  id: UUID,
  flowId: UUID,
  folio: String,
  contentType: String,
  submitter: UUID,
  task: Task,
  activityHis: Seq[ActivityHis] = Seq.empty,
  isActive: Boolean = true
)