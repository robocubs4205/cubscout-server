package com.robocubs4205.cubscout


import chandu0101.scalajs.react.components.materialui.MuiMuiThemeProvider
import com.robocubs4205.cubscout.i18n._
import japgolly.scalajs.react.extra.router.{BaseUrl, Router}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

import scala.annotation.tailrec
import scala.scalajs.js.JSApp

object Main extends JSApp {
  def main() = {
    val foundContainer = document.getElementById("container")
    val container = if (foundContainer == null) {
      println("creating container since none found")
      val container = document.createElement("div")
      container.id = "container"
      document.body.appendChild(container)
      container
    } else foundContainer

    def shortOrdinal(i: Int): Interpolator = new Interpolator {
      override def apply(values: Either[Key, String]*) = {
        def suffix(i:Int) = i%10 match {
          case 1 => "st"
          case 2 => "nd"
          case 3 => "rd"
          case _ => "th"
        }
        i.toString+suffix(i)
      }
      override def arity = 0
    }

    val i18n = I18n(
      Configuration(
        Map(Locale("en", "us") -> LocaleConfiguration(
          Map(
            "field"~>"username"->"Username",
            "field"~>"password"->"Password",
            "field"~>"error"~>"required"->"Required",
            "button"~>"submit"->"Submit"
          ),
          Map(),
          Map("ordinal" ~> "short" -> shortOrdinal),
          Map()
        )),
        Map(),
        Locale("en", "us")
      ),
      Locale("en", "us")
    )

    MuiMuiThemeProvider()(
      Router(BaseUrl.fromWindowUrl(v => {
        v.split("#").head
      }), RouterConfig(i18n))()
    ).renderIntoDOM(container)
  }
}