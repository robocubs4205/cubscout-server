package com.robocubs4205.cubscout

import java.time.Instant

import com.netaporter.uri.Uri
import org.scalajs.dom

import scala.scalajs.js.Date

/**
  * An Object for accessing cookies
  * the characters ';' and '=' may not be used as cookie keys or values, a key or value cannot
  * begin or end with a space. Note that spaces in other positions are
  * valid and not treated as illegal characters. The Cookie
  * object attempts to provide reasonable behaviour when calling code uses
  * illegal characters: The set functions automatically strip these characters before saving, the
  * apply(String) function gets the cookie which matches the supplied string ignoring the
  * illegal characters, and the compare function ignores these illegal characters
  *
  */
object Cookies {
  def apply(): Map[String, String] = {
    dom.document.cookie.split(';').map {
      cookieString =>
        val parts = cookieString.split('=')
        (parts.headOption, parts.tail.headOption)
    }.filter(_._1.isDefined).filter(_._2.isDefined).map(p => (p._1.get, p._2.get))
      //browsers may or may not trim spaces at the beginning and/or end of keys and/or values,
      //so they are trim here for consistency
      .map(p => (p._1.trim, p._2.trim))
      .toMap
  }

  def apply(key: String): Option[String] = apply().get(stripIllegalChars(key))

  def update(key: String, value: String): Unit = set(key, value)

  def set(key: String, value: String): Unit = {
    dom.document.cookie = s"${stripIllegalChars(key)}=${stripIllegalChars(value)}"
  }

  def compare(key: String, value: String): Boolean = apply(key).contains(stripIllegalChars(value))

  def set(key: String, value: String, expiration: Instant, path: Uri): Unit = {
    val expirationString = new Date(expiration.toString).toUTCString()
    dom.document.cookie =
      s"${stripIllegalChars(key)}=${stripIllegalChars(value)};expires=$expirationString;path=${path.path.replaceFirst(" ", "/")}"
  }

  def set(key: String, value: String, expiration: Instant): Unit = {
    val expirationString = new Date(expiration.toString).toUTCString()
    dom.document.cookie =
      s"${stripIllegalChars(key)}=${stripIllegalChars(value)};expires=$expirationString"
  }

  def set(key: String, value: String, path: Uri): Unit = {
    dom.document.cookie =
      s"${stripIllegalChars(key)}=${stripIllegalChars(value)};path=${path.path.replaceFirst(" ", "/")}"
  }

  def unset(key: String, path: String = "/"): Unit = {
    dom.document.cookie = s"""${stripIllegalChars(key)}="";path=${stripIllegalChars(path)}"""
  }

  private def stripIllegalChars(string: String): String = string.filter(!Seq('=', ';').contains(_)).trim
}
