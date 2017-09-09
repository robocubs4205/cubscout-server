package com.robocubs4205.cubscout.i18n

case class TranslationNotFoundError(key: Key)
  extends RuntimeException(s"No translation found for key $key")
