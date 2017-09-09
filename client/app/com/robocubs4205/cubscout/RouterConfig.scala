package com.robocubs4205.cubscout

import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.Implicits._

object RouterConfig {

  import com.robocubs4205.cubscout.RouterConfig.Page._

  val routerConfig = RouterConfigDsl[Page].buildConfig {
    dsl =>
      import dsl._
      {
        emptyRule |
          staticRoute("#login", Login) ~> render(LoginComponent()) |
          staticRoute("#scout", ScoutPage) ~> render(MainComponent())
      }.fallback(_ => Path("#"), (_, _) => redirectToPage(ScoutPage)(Redirect.Replace))
        .notFound(path => redirectToPage(ScoutPage)(Redirect.Replace))
  }

  sealed trait Page {

  }

  private object Page {

    case object Login extends Page

    case object ScoutPage extends Page

    case object EventResultPage extends Page

    case class NotFound(url: String) extends Page

  }

  def apply() = routerConfig
}
