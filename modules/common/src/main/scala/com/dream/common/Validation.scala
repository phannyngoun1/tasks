package com.dream.common

import cats.data.ValidatedNel
import cats.syntax.validated._
import simulacrum._
import scala.language.implicitConversions

object TypeValidator {

  implicit val notEmptyString: String => Boolean = value => value.nonEmpty

}

object Validation {

  type ValidationResult[A] = ValidatedNel[Error, A]

  sealed trait Error {
    val message: String
    val cause: Option[Throwable]
  }

  case class RequiredError(message: String, cause: Option[Throwable] = None) extends Error

  def validateNotEmptyString(key: String, value: String): ValidationResult[String] = {
    if (value.nonEmpty)
      value.validNel
    else
      RequiredError("Empty string").invalidNel
  }


  def validateMandatory[A](key: String, value: A)(implicit f: A => Boolean): ValidationResult[A] = {
    if (f(value))
      value.validNel
    else
      RequiredError(s"$key is empty").invalidNel
  }


  @typeclass trait Validator[A] {
    def validate(a: A): ValidationResult[A]
  }

  object Validator {
    def doValidate[A](f: A => ValidationResult[A]): Validator[A] = new Validator[A] {
      override def validate(a: A): ValidationResult[A] = f(a)
    }
  }

}