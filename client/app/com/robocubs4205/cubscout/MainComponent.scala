package com.robocubs4205.cubscout

import chandu0101.scalajs.react.components.materialui.{Mui, MuiIconButton, MuiIconMenu, MuiMenuItem, MuiMuiThemeProvider, MuiPalette, MuiRaisedButton, MuiRawTheme, MuiTheme}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import chandu0101.scalajs.react.components.materialui.MuiSvgIcon._

import scala.scalajs.js

object MainComponent {
  val theme = Mui.Styles.getMuiTheme(Mui.Styles.LightRawTheme.copy(
    palette = MuiPalette(
      primary1Color = Mui.Styles.colors.indigo500,
      primary2Color = Mui.Styles.colors.cyan500,
      primary3Color = Mui.Styles.colors.red500,
      accent1Color = Mui.Styles.colors.red400,
      accent2Color = Mui.Styles.colors.red400,
      accent3Color = Mui.Styles.colors.red400,
      textColor = Mui.Styles.colors.black,
      alternateTextColor = Mui.Styles.colors.grey100,
      canvasColor = Mui.Styles.colors.grey50,
      borderColor = Mui.Styles.colors.amber400,
      disabledColor = Mui.Styles.colors.grey100
    )
  ))

  val component = ScalaComponent.static("Main")(
    MuiMuiThemeProvider(muiTheme = theme)(
      NavComponent()
    )
  )

  def apply() = component()
}
