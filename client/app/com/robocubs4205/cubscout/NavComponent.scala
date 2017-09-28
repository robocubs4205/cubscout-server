package com.robocubs4205.cubscout

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js
import chandu0101.scalajs.react.components.materialui._
import chandu0101.scalajs.react.components.materialui.MuiSvgIcon._

object NavComponent {
  val component = ScalaComponent.static("Nav")(
    MuiAppBar(
      title = vdomNodeFromString("CubScout"),
      iconElementLeft = vdomElementFromComponent({
        MuiIconMenu(
          desktop = true,
          iconButtonElement = MuiIconButton(iconStyle = new js.Object(){
            val fill = Mui.Styles.colors.white
          })(Mui.SvgIcons.NavigationMenu()())
        )(
          MuiMenuItem(
            primaryText = vdomNodeFromString("foo"),
            onTouchTap = (_:TouchTapEvent) => Callback{
              println("menu clicked")
            }
          )()
        )
      }),
      onLeftIconButtonTouchTap = (_: ReactEvent) => Callback {
        println("touched")
      }
    )()
  )

  def apply() = component()
}
