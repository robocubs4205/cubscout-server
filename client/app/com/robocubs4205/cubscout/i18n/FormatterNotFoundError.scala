package com.robocubs4205.cubscout.i18n

import scala.reflect.ClassTag

case class FormatterNotFoundError(requiredClassTag: ClassTag[_])
  extends RuntimeException(s"No formatters were found that can format ${requiredClassTag.toString()}")
