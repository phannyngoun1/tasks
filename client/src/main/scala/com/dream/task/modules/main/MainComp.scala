package com.dream.task.modules.main

import com.dream.task.AppClient.Loc
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._

object MainComp {

  case class Props(c: RouterCtl[Loc], r: Resolution[Loc])

  case class State()

  private val component = ScalaComponent.builder[Props]("DashboardComp")
    .initialState(State())
    .renderP {(_, p) =>
      <.div(p.r.render())
    }
    .build

  def apply(props: Props) = component(props)

}
