package com.dream.task.services

import com.dream.task.modules.dashboard.DashboardHandler
import com.dream.task.services.DataModel.AppModel
import diode._
import diode.react.ReactConnector

/**
  * AppCircuit provides the actual instance of the `AppModel` and all the action
  * handlers we need. Everything else comes from the `Circuit`
  */
object AppCircuit extends Circuit[AppModel] with ReactConnector[AppModel] {
  // define initial value for the application model
  def initialModel = AppModel("Hello")

  override val actionHandler = composeHandlers(
    new DashboardHandler(zoomTo(_.value)
  ))
}