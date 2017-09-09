package com.robocubs4205.cubscout.i18n

case class Configuration(
  localeConfigs: Map[Locale,LocaleConfiguration],
  fallbacks: Map[Either[Locale,String],Either[Locale,String]],
  defaultLocale:Locale
) {
  def +(configuration: Configuration) = Configuration(
    for{
      (locale1,config1) <- localeConfigs
      (locale2,config2) <- configuration.localeConfigs
      if locale1==locale2
    } yield locale1 -> (config1+config2),
    fallbacks ++ configuration.fallbacks,
    defaultLocale
  )
}
