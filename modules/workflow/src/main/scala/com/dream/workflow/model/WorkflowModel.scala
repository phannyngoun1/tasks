package com.dream.workflow.model

import java.util.UUID

import com.dream.common.Model.ResponseJson
import play.api.libs.json.{Format, Json}

object WorkflowModel {

  case class CreateItemJson  (
    id: UUID,
    errorMessages: Seq[String] = Seq.empty
  ) extends ResponseJson {
    override val isSuccessful: Boolean = errorMessages.isEmpty
  }

  object CreateItemJson {
    implicit val format: Format[CreateItemJson] = Json.format
  }

  case class ItemJson(
    id: UUID,
    name: String,
    desc: String,
    workflowId: UUID
  )

  object ItemJson {
    implicit val format: Format[ItemJson] = Json.format
  }

  case class ParticipantJson(
    id: UUID,
    accountId: UUID,
    teamID: UUID,
    departmentId: UUID,
    propertyId: UUID,
    isActive: Boolean = true,
    isDeleted: Boolean = false,
    tasks: List[String]
  )

  object ParticipantJson {
    implicit val format: Format[ParticipantJson] = Json.format
  }
}
