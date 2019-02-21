package com.dream.common

object Model {

  trait ResponseJson {
    val isSuccessful: Boolean
    val errorMessages: Seq[String]
  }

}
