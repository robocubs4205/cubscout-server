package com.robocubs4205.cubscout.i18n

/**
  * A class representing the i18n configuration for a particular locale
  *
  * @param keys         A map from keys to Interpolators. Falls back to fallbackKeys values for parent keys, checking
  *                     closest ancestors first
  * @param fallbackKeys A map from keys to Interpolators. Can fall back to other locales or fail to translate
  * @param pluralKeys   A map from keys to functions that return Interpolators representing plurals given a count of objects.
  *                     Falls back to ignoring the count and searching the keys map
  * @param formatters   A map from keys to formatters that can format various types
  */
case class LocaleConfiguration(
  keys: Map[Key, Interpolator],
  fallbackKeys: Map[Key, Interpolator],
  pluralKeys: Map[Key, (Int) => Interpolator],
  formatters: Map[Key, Formatter[_]]
){
  def +(localeConfiguration: LocaleConfiguration) = LocaleConfiguration(
    keys ++ localeConfiguration.keys,
    fallbackKeys ++ localeConfiguration.fallbackKeys,
    pluralKeys ++ localeConfiguration.pluralKeys,
    formatters ++ localeConfiguration.formatters
  )
}
