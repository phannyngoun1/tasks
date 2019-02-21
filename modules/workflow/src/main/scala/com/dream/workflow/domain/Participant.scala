package com.dream.workflow.domain

import java.time.Instant
import java.util.UUID

import com.dream.common.domain.ErrorMessage
import com.dream.workflow.domain.Participant.{DefaultParticipantError, ParticipantError, ParticipantEvent, TaskAssigned}
import play.api.libs.json.{Format, Json}

case class Team(name: String)

object Team {
  implicit val format: Format[Team] = Json.format
}

case class Department(name: String)

object Department {
  implicit val format: Format[Department] = Json.format
}

case class Company(name: String)

object Company {
  implicit val format: Format[Company] = Json.format
}

case class ParticipantTask(
  taskId: UUID,
  pInstId: UUID
)

case class ParticipantAccess(participantId: UUID)

object Participant {

  sealed trait ParticipantError extends ErrorMessage

  case class DefaultParticipantError(message: String) extends ParticipantError

  case class InvalidParticipantStateError(override val id: Option[UUID] = None) extends ParticipantError {
    override val message: String = s"Invalid state${id.fold("")(id => s":id = ${id.toString}")}"
  }

  sealed trait ParticipantEvent

  case class ParticipantCreated(
    id: UUID,
    accountId: UUID,
    teamId: UUID,
    departmentId: UUID,
    propertyId: UUID
  ) extends ParticipantEvent

  case class TaskAssigned(
    id: UUID,
    taskId: UUID,
    pInstId: UUID,

  ) extends ParticipantEvent

  case class TaskPerformed(
    activity: BaseActivity,
    action: BaseAction,
    activityId: UUID,
    by: UUID,
    date: Instant
  )
}

case class Participant(
  id: UUID,
  accountId: UUID,
  teamID: UUID,
  departmentId: UUID,
  propertyId: UUID,
  isActive: Boolean = true,
  isDeleted: Boolean = false,
  tasks: List[ParticipantTask] = List.empty,
  taskHist: List[ParticipantTask] = List.empty
) {

  def assignTask(taskId: UUID, pInstId: UUID) : Either[ParticipantError, Participant] =

    if(!tasks.exists(_.taskId.equals(taskId)))
      Right(copy(
        tasks = ParticipantTask(
          taskId = taskId,
          pInstId = pInstId
        ) :: tasks
      ))
    else
      Left(DefaultParticipantError("Task is already assigned"))


}




