package com.robocubs4205.cubscout.i18n

trait Interpolator {
  def arity: Int
  def apply(values: Either[Key,String]*) : String
}
