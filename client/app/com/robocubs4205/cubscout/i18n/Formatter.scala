package com.robocubs4205.cubscout.i18n
import scala.reflect.ClassTag

trait Formatter[-T] {
  def clazz: ClassTag[_]
  def apply(t:T):String
}
