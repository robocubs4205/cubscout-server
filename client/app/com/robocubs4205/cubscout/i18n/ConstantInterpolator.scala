package com.robocubs4205.cubscout.i18n

case class ConstantInterpolator(value:String) extends Interpolator{
  override val arity = 0
  override def apply(values: Either[Key,String]*) = value
}
