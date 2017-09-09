package com.robocubs4205.cubscout.i18n

import scala.language.implicitConversions

case class Key(value:String) {
  def ->(key:Key) = Key(s"$value.${key.value}")
  def parent: Option[Key] = {
    val parentString = value.split("\\.").dropRight(1).mkString(".")
    if (parentString.isEmpty) None else Some(Key(parentString))
  }
  override def toString = value
}

object Key{
  implicit def stringToKey(s:String) = Key(s)
  implicit def keyToString(k:Key) = k.value
}
