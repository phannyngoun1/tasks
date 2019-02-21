package com.dream.common

import com.dream.common.domain.ErrorMessage

object Protocol {

  trait CmdRequest

  trait CmdResponse


  case class CmdResponseFailed(errorMessage: ErrorMessage)

}
