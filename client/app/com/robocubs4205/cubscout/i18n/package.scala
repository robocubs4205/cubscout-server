package com.robocubs4205.cubscout

import scala.language.implicitConversions

package object i18n {
  implicit def stringToKey(str:String) = Key(str)
  implicit def stringToConstantInterpolator(str:String) = ConstantInterpolator(str)
}
