package com.dream.task.modules.dashboard

import com.dream.task.AppClient.Loc
import diode.react.ModelProxy
import japgolly.scalajs.react.ScalaComponent
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._

object DashboardComp {

  case class Props(proxy: ModelProxy[String], c: RouterCtl[Loc])

  case class State(txt: String)

  class Backend($: BackendScope[Props, State]) {
    def render(p: Props, s: State) = {
      <.div(s.txt)
    }
  }

  private val component = ScalaComponent.builder[Props]("DashboardComp")
    .initialStateFromProps(p => State(p.proxy.value))
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[String], c: RouterCtl[Loc]) = component(Props(proxy, c))
}
