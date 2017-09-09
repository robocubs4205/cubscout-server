package com.robocubs4205.cubscout.i18n

case class WrongInterpolatorArityError(found: Int, required: Int)
  extends RuntimeException(
    s"At least one interpolator was found for the key, but did not have the correct arity " +
      s"(was not able to handle the number of values given) " +
      s"arity of a found interpolator:$found, required arity:$required")
