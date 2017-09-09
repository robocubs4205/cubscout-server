package com.robocubs4205.cubscout


import chandu0101.scalajs.react.components.materialui.MuiMuiThemeProvider
import japgolly.scalajs.react.extra.router.{BaseUrl, Router}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

import scala.scalajs.js.JSApp

object Main extends JSApp{
  def main() = {
    val foundContainer = document.getElementById("container")
    val container = if (foundContainer == null ) {
      println("creating container since none found")
      val container = document.createElement("div")
      container.id = "container"
      document.body.appendChild(container)
      container
    } else foundContainer
    MuiMuiThemeProvider()(
      Router(BaseUrl.fromWindowOrigin,RouterConfig())()
    ).renderIntoDOM(container)
  }
}