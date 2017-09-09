package com.robocubs4205.cubscout

import chandu0101.scalajs.react.components.materialui.MuiTextField
import japgolly.scalajs.react.{Callback, ScalaComponent}
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

import com.robocubs4205.cubscout

import scala.scalajs.js

object LoginComponent {

  val component = ScalaComponent.builder[Unit]("Login").initialState(State()).renderBackend[Backend].build

  case class State(
    usernameMissing: Boolean = false,
    passwordMissing: Boolean = false,
    wrongCredentials: Boolean = false,
    username: String = "",
    password: String = ""
  )

  case class Backend($: BackendScope[Unit, State]) {

    def submit(e: ReactEvent) = Callback {

    }

    def onChangeUsername(event: ReactEventFromInput, str: String) =
      $.modState(s => s.copy(username = str)) >> Callback{
        println(str)
      }

    def usernameField(s: State) =
      if (s.usernameMissing) MuiTextField(
        floatingLabelText = vdomNodeFromString("Username"),
        errorText = vdomNodeFromString("required"),
        value = s.username,
        onChange = (e: ReactEventFromInput, s: String) => onChangeUsername(e, s)
      )()
      else MuiTextField(
        floatingLabelText = vdomNodeFromString("Username"),
        value = s.username,
        onChange = (e: ReactEventFromInput, s: String) => onChangeUsername(e, s)
      )()

    def passwordField(s: State) =
      if (s.passwordMissing) MuiTextField(floatingLabelText = vdomNodeFromString("Password"), errorText = vdomNodeFromString("required"))()
      else MuiTextField(floatingLabelText = vdomNodeFromString("Password"))()

    def render(s: State) = <.form(
      ^.onSubmit ==> ((e: ReactEvent) => submit(e))
    )(
      usernameField(s),
      passwordField(s)()
    )
  }

  def apply() = component()

}
