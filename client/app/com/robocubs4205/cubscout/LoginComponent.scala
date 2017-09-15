package com.robocubs4205.cubscout

import chandu0101.scalajs.react.components.materialui.{MuiRaisedButton, MuiTextField}
import japgolly.scalajs.react.{Callback, ScalaComponent}
import japgolly.scalajs.react.component.Scala.BackendScope
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.ScalazReact._
import com.robocubs4205.cubscout
import com.robocubs4205.cubscout.i18n._

import org.scalajs.dom

object LoginComponent {

  val component = ScalaComponent.builder[I18n]("Login").initialState(State()).renderBackend[Backend].build

  case class State(
    usernameMissing: Boolean = false,
    passwordMissing: Boolean = false,
    wrongCredentials: Boolean = false,
    username: String = "",
    password: String = ""
  )

  case class Backend($: BackendScope[I18n, State]) {

    val ST = ReactS.Fix[State]

    def submit(e: ReactEvent) = onBlurUsername >> onBlurPassword >> ST.mod{
      state =>
        if(!state.usernameMissing && !state.passwordMissing) dom.window.alert("You did it!")
        state
    }

    def onChangeUsername(str: String) =
      ST.mod(s=>s.copy(username = str)) >> ST.mod(s=> if(s.username!="") s.copy(usernameMissing = false) else s)


    def onChangePassword(str: String) =
      ST.mod(s=>s.copy(password = str)) >> ST.mod(s=> if(s.password!="") s.copy(usernameMissing = false) else s)


    val onBlurUsername =
      ST.mod(s => if(s.username=="") s.copy(usernameMissing = true) else s.copy(usernameMissing = false))

    val onBlurPassword =
      ST.mod(s => if(s.password=="") s.copy(passwordMissing = true) else s.copy(passwordMissing = false))

    def usernameField(i18n: I18n, s: State) = {
      val label = i18n("field" ~> "username").get
      val errorLabel = i18n("field" ~> "error" ~> "required").get
      if (s.usernameMissing) MuiTextField(
        floatingLabelText = vdomNodeFromString(label),
        errorText = vdomNodeFromString(errorLabel),
        value = s.username,
        onChange = (_: ReactEventFromInput, s: String) => $.runState(onChangeUsername(s)),
        onBlur = (_:ReactFocusEventFromInput) => $.runState(onBlurUsername)
      )()
      else MuiTextField(
        floatingLabelText = vdomNodeFromString(label),
        value = s.username,
        onChange = (_: ReactEventFromInput, s: String) => $.runState(onChangeUsername(s)),
        onBlur = (_:ReactFocusEventFromInput) => $.runState(onBlurUsername)
      )()
    }

    def passwordField(i18n: I18n, s: State) = {
      val label = i18n("field"~>"password").get
      val errorLabel = i18n("field" ~> "error" ~> "required").get
      if (s.passwordMissing) MuiTextField(
        floatingLabelText = vdomNodeFromString(label),
        errorText = vdomNodeFromString(errorLabel),
        onChange = (_:ReactEventFromInput, s:String) => $.runState(onChangePassword(s)),
        onBlur = (_:ReactFocusEventFromInput) => $.runState(onBlurPassword),
        `type` = "password")()
      else MuiTextField(
        floatingLabelText = vdomNodeFromString(label),
        onChange = (_:ReactEventFromInput, s:String) => $.runState(onChangePassword(s)),
        onBlur = (_:ReactFocusEventFromInput) => $.runState(onBlurPassword),
        `type` = "password"
      )()
    }

    def render(i18n: I18n, s: State) = <.form(
      ^.onSubmit ==> $.runStateFn(submit),
      ^.display.flex,
      ^.flexDirection.column,
      ^.alignItems.center
    )(
      usernameField(i18n, s),
      passwordField(i18n, s),
      MuiRaisedButton(label = i18n("button"~>"submit").get, `type` = "submit")()
    )
  }

  def apply(i18n:I18n) = component(i18n)

}
