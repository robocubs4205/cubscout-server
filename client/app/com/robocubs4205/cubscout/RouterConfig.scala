package com.robocubs4205.cubscout

import com.robocubs4205.cubscout.i18n.I18n
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.Implicits._

object RouterConfig {

  import com.robocubs4205.cubscout.RouterConfig.Page._

  def routerConfig(i18n:I18n) = RouterConfigDsl[Page].buildConfig {
    dsl =>
      import dsl._
      {
        emptyRule |
          staticRoute("#login", Login) ~> render(LoginComponent(i18n)) |
          staticRoute("#scout", ScoutPage) ~> render(MainComponent())
      }.fallback(_ => Path("#"), (_, _) => redirectToPage(Login)(Redirect.Replace))
        .notFound(_ => redirectToPage(Login)(Redirect.Replace))
  }

  sealed trait Page {

  }

  private object Page {

    case object Login extends Page

    case object ScoutPage extends Page

    case object EventResultPage extends Page

    case class NotFound(url: String) extends Page

  }

  def apply(i18n:I18n) = routerConfig(i18n)
}
