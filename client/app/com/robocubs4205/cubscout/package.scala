package com.robocubs4205

import japgolly.scalajs.react._
import japgolly.scalajs.react.raw.{ReactNode, ReactNodeList}
import japgolly.scalajs.react.vdom.html_<^._

import scala.scalajs.js.UndefOr._

import scala.language.implicitConversions

package object cubscout {
  implicit def LongToUndefOrVdomNode(v: Long) = any2undefOrA(vdomNodeFromLong(v))

  implicit def IntToUndefOrVdomNode(v: Int) = any2undefOrA(vdomNodeFromInt(v))

  implicit def ShortToUndefOrVdomNode(v: Short) = any2undefOrA(vdomNodeFromShort(v))

  implicit def ByteToUndefOrVdomNode(v: Byte) = any2undefOrA(vdomNodeFromByte(v))

  implicit def DoubleToUndefOrVdomNode(v: Double) = any2undefOrA(vdomNodeFromDouble(v))

  implicit def FloatToUndefOrVdomNode(v: Float) = any2undefOrA(vdomNodeFromFloat(v))

  implicit def StringToUndefOrVdomNode(v: String) = any2undefOrA(vdomNodeFromString(v))

  implicit def ReactNodeToUndefOrVdomNode(v: ReactNode) = any2undefOrA(vdomNodeFromReactNode(v))

  implicit def ReactNodeListToUndefOrVdomNode(v: ReactNodeList) = any2undefOrA(vdomNodeFromReactNodeList(v))

  implicit def PropsChildrenToUndefOrVdomNode(v: PropsChildren) = any2undefOrA(vdomNodeFromPropsChildren(v))

}
