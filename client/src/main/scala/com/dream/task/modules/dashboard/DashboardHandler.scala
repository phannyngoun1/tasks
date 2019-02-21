package com.dream.task.modules.dashboard

import diode.util.RunAfterJS
import diode.{Action, ActionHandler, ActionResult, ModelRW}


object DashboardHandler {

  case object InitAction extends Action

}

class DashboardHandler[M](modelRW: ModelRW[M, String]) extends ActionHandler(modelRW) {
  implicit val runner = new RunAfterJS

  import DashboardHandler._

  override protected def handle: PartialFunction[Any, ActionResult[M]] = {
    case InitAction => updated(value)
  }

}
