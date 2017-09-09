package com.robocubs4205.cubscout.i18n

import scala.annotation.tailrec
import scala.collection.mutable
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

final case class I18n(configuration: Configuration, locale: Locale) {
  def plural(key: Key, count: Int, values: Either[Key, String]*): Try[String] = {
    configuration.localeConfigs.get(locale).flatMap(localePlural(key, count, values.length, _)).map(_ (values: _*))
      .map(Success(_)) getOrElse
      findFallbackPluralInterpolator(key, count, values.length, localeFallbacks).map(_ (values: _*))
  }

  def format[T](key: Key, value: T)(implicit classTag: ClassTag[T]): Try[String] = {
    configuration.localeConfigs.get(locale).flatMap(_.formatters.get(key))
      .filter(_.clazz.runtimeClass.isAssignableFrom(classTag.runtimeClass)).map(_.asInstanceOf[Formatter[T]])
      .map(_ (value)).map(Success(_)) getOrElse
      findFallbackFormatter(key, localeFallbacks).map(_.asInstanceOf[Formatter[T]]).map(_ (value))
  }

  def apply(key: Key, values: Either[Key, String]*): Try[String] = {
    configuration.localeConfigs.get(locale).flatMap(localeInterpolator(key, values.length, _))
      .map(_ (values: _*)).map(Success(_)) getOrElse
      findFallbackInterpolator(key, values.length, localeFallbacks).map(_ (values: _*))
  }

  //the fallbacks of the configured locale, in order, not including the configured locale
  private def localeFallbacks: Iterator[LocaleConfiguration] = new Iterator[LocaleConfiguration] {
    val attemptedLocales = mutable.Buffer[Locale](locale)
    var fallbackFrom = locale

    //noinspection VariablePatternShadow,UnitInMap
    override def hasNext = (configuration.fallbacks.get(Left(fallbackFrom)).filter {
        //fall back to configured locale fallback
        case Left(locale) => !attemptedLocales.contains(locale) && configuration.localeConfigs.isDefinedAt(locale)
        case Right(lang) =>
          configuration.localeConfigs.keys.filter(_.languageCode == lang).exists(!attemptedLocales.contains(_))
      } orElse
      //then fall back to locales with the same language
      configuration.localeConfigs.keys.filter(_.languageCode == fallbackFrom.languageCode)
        .find(!attemptedLocales.contains(_)) orElse
      //finally fall back to configured language fallback
      configuration.fallbacks.get(Right(fallbackFrom.languageCode)).filter {
        case Left(locale) => !attemptedLocales.contains(locale) && configuration.localeConfigs.isDefinedAt(locale)
        case Right(lang) =>
          configuration.localeConfigs.keys.filter(_.languageCode == lang).exists(!attemptedLocales.contains(_))
      }).map(_ => true) getOrElse (!attemptedLocales.contains(configuration.defaultLocale) &&
      configuration.localeConfigs.isDefinedAt(configuration.defaultLocale))

    //noinspection VariablePatternShadow
    override def next() = {
      (configuration.fallbacks.get(Left(fallbackFrom)).filter {
        //fall back to configured locale fallback
        case Left(locale) => !attemptedLocales.contains(locale) && configuration.localeConfigs.isDefinedAt(locale)
        case Right(lang) =>
          configuration.localeConfigs.keys.filter(_.languageCode == lang)
            .exists(!attemptedLocales.contains(_))
      }.map {
        case Left(locale) => locale
        case Right(lang) => configuration.localeConfigs.keys.filter(_.languageCode == lang)
          .filter(!attemptedLocales.contains(_)).head
      }.map {
        locale =>
          attemptedLocales += locale
          fallbackFrom = locale
          locale
      } orElse
        //then fall back to locales with the same language
        configuration.localeConfigs.keys.filter(_.languageCode == fallbackFrom.languageCode)
          .find(!attemptedLocales.contains(_)).map {
          locale =>
            attemptedLocales += locale
            fallbackFrom = locale
            locale
        } orElse configuration.fallbacks.get(Right(fallbackFrom.languageCode)).filter {
        //finally fall back to configured language fallback
        case Left(locale) => !attemptedLocales.contains(locale) && configuration.localeConfigs.isDefinedAt(locale)
        case Right(lang) =>
          configuration.localeConfigs.keys.filter(_.languageCode == lang).exists(!attemptedLocales.contains(_))
      }.map {
        case Left(locale) => locale
        case Right(lang) => configuration.localeConfigs.keys.filter(_.languageCode == lang)
          .filter(!attemptedLocales.contains(_)).head
      }.map {
        locale =>
          attemptedLocales += locale
          fallbackFrom = locale
          locale
      } orElse Some(configuration.defaultLocale).filter(!attemptedLocales.contains(_)))
        .flatMap(configuration.localeConfigs.get).getOrElse(throw new IllegalStateException("next called on fallback chain, but no more fallbacks!"))

    }
  }

  @tailrec
  private def findFallbackFormatter[T](
    key: Key,
    fallbackChain: Iterator[LocaleConfiguration]
  )(implicit classTag: ClassTag[T]): Try[Formatter[T]] = {
    if (fallbackChain.hasNext) {
      val maybeFormatter = fallbackChain.next().formatters.get(key)
      if (maybeFormatter.isDefined) {
        if (maybeFormatter.get.clazz.runtimeClass.isAssignableFrom(classTag.runtimeClass))
          Success(maybeFormatter.get.asInstanceOf[Formatter[T]])
        else findFallbackFormatter(key, fallbackChain)
      }
      else findFallbackFormatter(key, fallbackChain)
    }
    else Failure(FormatterNotFoundError(classTag))
  }

  @tailrec
  private def findFallbackInterpolator(
    key: Key,
    requiredArity: Int,
    fallbackChain: Iterator[LocaleConfiguration],
    wrongArity: Option[Int] = None
  ): Try[Interpolator] = {
    if (fallbackChain.hasNext) {
      val maybeInterpolator = localeInterpolator(key, requiredArity, fallbackChain.next())
      if (maybeInterpolator.isDefined) {
        val interpolator = maybeInterpolator.get
        if (interpolator.arity == requiredArity) Success(interpolator)
        else findFallbackInterpolator(key, requiredArity, fallbackChain, Some(interpolator.arity))
      }
      else findFallbackInterpolator(
        key,
        requiredArity,
        fallbackChain,
        wrongArity
      )
    }
    else if (wrongArity.isDefined) Failure(WrongInterpolatorArityError(wrongArity.get, requiredArity))
    else Failure(TranslationNotFoundError(key))
  }

  private def localeInterpolator(key: Key, requiredArity: Int, localeConfig: LocaleConfiguration): Option[Interpolator] = {
    localeConfig.keys.get(key).filter(_.arity == requiredArity) orElse
      fallbackCheck(key, requiredArity, localeConfig.fallbackKeys)
  }

  @tailrec
  private def findFallbackPluralInterpolator(
    key: Key,
    count: Int,
    requiredArity: Int,
    fallbackChain: Iterator[LocaleConfiguration],
    wrongArity: Option[Int] = None
  ): Try[Interpolator] = {
    if (fallbackChain.hasNext) {
      val maybeInterpolator = localePlural(key, count, requiredArity, fallbackChain.next())
      if (maybeInterpolator.isDefined) {
        val interpolator = maybeInterpolator.get
        if (interpolator.arity == requiredArity) Success(interpolator)
        else findFallbackPluralInterpolator(key, count, requiredArity, fallbackChain, Some(interpolator.arity))
      }
      else findFallbackPluralInterpolator(
        key,
        count,
        requiredArity,
        fallbackChain,
        wrongArity
      )
    }
    else if (wrongArity.isDefined) Failure(WrongInterpolatorArityError(wrongArity.get, requiredArity))
    else Failure(TranslationNotFoundError(key))
  }

  def localePlural(key: Key, count: Int, requiredArity: Int, localeConfig: LocaleConfiguration): Option[Interpolator] = {
    localeConfig.pluralKeys.get(key).map(_ (count)).filter(_.arity == requiredArity) orElse
      localeInterpolator(key, requiredArity, localeConfig)
  }

  @tailrec
  private def fallbackCheck(key: Key, requiredArity: Int, map: Map[Key, Interpolator]): Option[Interpolator] = {
    val maybeInterpolator = map.get(key)
    if (maybeInterpolator.isDefined && maybeInterpolator.get.arity == requiredArity) maybeInterpolator
    else {
      val maybeParent = key.parent
      if (maybeParent.isEmpty) None else fallbackCheck(maybeParent.get, requiredArity, map)
    }
  }

}
