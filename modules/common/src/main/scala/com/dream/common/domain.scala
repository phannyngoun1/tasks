package com.dream.common

import java.util.UUID

object domain {

  trait Type {

    def value: String

    def name: String

    def description: Option[String]

  }

  trait ActivityTracking {

  }

  trait ErrorMessage {
    val id: Option[UUID] = None
    val message: String

  }

  case class ResponseError(override val id: Option[UUID], message: String) extends ErrorMessage

  object ResponseError {
    def apply(errorMessage: ErrorMessage): ResponseError = ResponseError(errorMessage.id, errorMessage.message )
  }


  trait Param


}
