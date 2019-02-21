package com.dream.task

import com.dream.task.modules.dashboard.DashboardComp
import com.dream.task.modules.main.MainComp
import com.dream.task.services.AppCircuit
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router._
import org.scalajs.dom

object AppClient {

  sealed trait Loc

  case object DashboardLoc extends Loc

  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._
    (
      staticRoute(root, DashboardLoc) ~> renderR(c => AppCircuit.wrap(_.value)(proxy => DashboardComp(proxy, c)))

      ).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))

  }.renderWith(layout)

  val mainWrapper = AppCircuit.connect(_.value)

  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {
    mainWrapper(proxy => MainComp(MainComp.Props(c, r)))
  }

  def main(args: Array[String]): Unit = {
    val router = Router(BaseUrl.until_#, routerConfig)
    router().renderIntoDOM(dom.document.getElementById("root"))
  }
}
