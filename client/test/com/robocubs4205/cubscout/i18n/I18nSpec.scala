package com.robocubs4205.cubscout.i18n


import java.io.{PrintWriter, StringWriter}

import org.scalatest.{MustMatchers, OptionValues, WordSpec}

import scala.language.postfixOps

class I18nSpec extends WordSpec with MustMatchers with OptionValues {
  "I18n" must {
    "fail to find a translation" when {
      "there are no translations" in {
        val i18n = I18n(
          Configuration(Map(), Map(), Locale("zhx", "cn")),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isFailure mustBe true
        result.failed.get mustBe TranslationNotFoundError(Key("foo"))
      }
      "There is a translation for another locale of a different language and there is no fallback to that locale" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("tai", "th") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isFailure mustBe true
        result.failed.get mustBe TranslationNotFoundError(Key("foo"))
      }
      //This scenario doesn't make sense but can happen
      "The locale falls back to itself and does not have the translation" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("tai", "th") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(Left(Locale("en","us"))->Left(Locale("en","us"))),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isFailure mustBe true
        result.failed.get mustBe TranslationNotFoundError(Key("foo"))
      }
      //This scenario doesn't make sense but can happen
      "The locale's language falls back to itself and no locales in that language have the translation" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("tai", "th") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(Right("en")->Right("en")),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isFailure mustBe true
        result.failed.get mustBe TranslationNotFoundError(Key("foo"))
      }
    }
    "return the translation" when {
      "There is a translation in this locale" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("en", "us") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      //This scenario doesn't make sense but can happen
      "There is a fallback translation for the key for this locale" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("en", "us") -> LocaleConfiguration(
              Map(),
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map()
            )),
            Map(),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      "There is a fallback translation for the parent key for this locale" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("en", "us") -> LocaleConfiguration(
              Map(),
              Map(Key("foo") -> ConstantInterpolator("foo")),
              Map(),
              Map()
            )),
            Map(),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo.bar"))
        result.isSuccess mustBe true
        result.get mustBe "foo"
      }
      "there is a fallback translation for a distant ancestor key for this locale" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("en", "us") -> LocaleConfiguration(
              Map(),
              Map(Key("foo") -> ConstantInterpolator("foo")),
              Map(),
              Map()
            )),
            Map(),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo.bar.baz"))
        result.isSuccess mustBe true
        result.get mustBe "foo"
      }
      "There is a translation for another locale and this locale falls back to that locale" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("tai", "th") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(Left(Locale("en", "us")) -> Left(Locale("tai", "th"))),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      "There is a translation for another locale and this locale's language falls back to that locale" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("tai", "th") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(Right("en") -> Left(Locale("tai", "th"))),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      "There is a translation for another locale and this locale falls back to that locale's language" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("tai", "th") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(Left(Locale("en", "us")) -> Right("tai")),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      "there is a translation for another locale and this locale's language falls back to that locale's language" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("tai", "th") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(Right("en") -> Right("tai")),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      "there is a translation for another locale with the same language" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("en", "uk") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      "There is a translation for another locale with the same language and there is no fallback to that locale" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("en", "uk") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      //making explicit an automatic fallback
      "A locale falls back to its own language and there is a translation for another locale with the same language" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("en", "uk") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(Left(Locale("en", "us")) -> Right("en")),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      //This scenario doesn't make sense but can happen
      "There is a translation for another locale with the same language and this locale falls back to itself" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("en", "uk") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(Left(Locale("en", "us")) -> Left(Locale("en", "us"))),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      //This scenario doesn't make sense but can happen
      "The locale's language falls back to itself and a locale in that language has the translation" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("en", "uk") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            )),
            Map(Right("en") -> Right("en")),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      //This scenario doesn't make sense but can happen
      "The locale's language falls back to itself and a locale in that language falls back to a locale that has the translation" in {
        val i18n = I18n(
          Configuration(
            Map(Locale("tai","th") -> LocaleConfiguration(
              Map(Key("foo") -> ConstantInterpolator("bar")),
              Map(),
              Map(),
              Map()
            ),
              Locale("en","uk") -> LocaleConfiguration(
                Map(),
                Map(),
                Map(),
                Map()
              )),
            Map(
              Right("en") -> Right("en"),
              Left(Locale("en","uk"))->Left(Locale("tai","th"))
            ),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
    }
    "prefer configured fallback to locales with the same language" when {
      "A locale has a fallback locale with a different language" in {
        val i18n = I18n(
          Configuration(
            Map(
              Locale("tai", "th") -> LocaleConfiguration(
                Map(Key("foo") -> ConstantInterpolator("bar")),
                Map(),
                Map(),
                Map()
              ),
              Locale("en", "uk") -> LocaleConfiguration(
                Map(Key("foo") -> ConstantInterpolator("baz")),
                Map(),
                Map(),
                Map()
              )),
            Map(Left(Locale("en", "us")) -> Left(Locale("tai", "th"))),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      "A locale has a fallback locale with the same language" in {
        val i18n = I18n(
          Configuration(
            Map(
              Locale("en", "de") -> LocaleConfiguration(
                Map(Key("foo") -> ConstantInterpolator("bar")),
                Map(),
                Map(),
                Map()
              ),
              Locale("en", "uk") -> LocaleConfiguration(
                Map(Key("foo") -> ConstantInterpolator("baz")),
                Map(),
                Map(),
                Map()
              )),
            Map(Left(Locale("en", "us")) -> Left(Locale("en", "de"))),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
      "A locale falls back to a different language" in {
        val i18n = I18n(
          Configuration(
            Map(
              Locale("tai", "th") -> LocaleConfiguration(
                Map(Key("foo") -> ConstantInterpolator("bar")),
                Map(),
                Map(),
                Map()
              ),
              Locale("en", "uk") -> LocaleConfiguration(
                Map(Key("foo") -> ConstantInterpolator("baz")),
                Map(),
                Map(),
                Map()
              )),
            Map(Left(Locale("en", "us")) -> Right("tai")),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "bar"
      }
    }
    "prefer locales with the same language to configured fallback" when {

      "A locale's language has a fallback locale with a different language" in {
        val i18n = I18n(
          Configuration(
            Map(
              Locale("tai", "th") -> LocaleConfiguration(
                Map(Key("foo") -> ConstantInterpolator("bar")),
                Map(),
                Map(),
                Map()
              ),
              Locale("en", "uk") -> LocaleConfiguration(
                Map(Key("foo") -> ConstantInterpolator("baz")),
                Map(),
                Map(),
                Map()
              )),
            Map(Right("en") -> Left(Locale("tai","th"))),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "baz"
      }
      "A locale's language has a fallback language and a locale with the same language exists" in {
        val i18n = I18n(
          Configuration(
            Map(
              Locale("tai", "th") -> LocaleConfiguration(
                Map(Key("foo") -> ConstantInterpolator("bar")),
                Map(),
                Map(),
                Map()
              ),
              Locale("en", "uk") -> LocaleConfiguration(
                Map(Key("foo") -> ConstantInterpolator("baz")),
                Map(),
                Map(),
                Map()
              )),
            Map(Right("en") -> Right("tai")),
            Locale("zhx", "cn")
          ),
          Locale("en", "us")
        )
        val result = i18n(Key("foo"))
        result.isSuccess mustBe true
        result.get mustBe "baz"
      }
    }

  }
}
