package com.robocubs4205.cubscout

import java.time.Instant

import com.netaporter.uri.Uri

import scalajs.js.Dynamic._

/**
  * An object for accessing cookies
  * URL encodes keys and values before storing, decodes when retrieving
  */
object EncodedCookies {
  def apply(): Map[String, String] = Cookies.apply().map {
    p => (global.decodeURIComponent(p._1).asInstanceOf[String],
      global.decodeURIComponent(p._2).asInstanceOf[String])
  }

  def apply(key: String): Option[String] = apply().get(key)

  def update(key: String, value: String): Unit = set(key, value)

  def set(key: String, value: String): Unit = Cookies.set(
    global.encodeURIComponent(key).asInstanceOf[String],
    global.encodeURIComponent(value).asInstanceOf[String]
  )

  def set(key: String, value: String, expiration: Instant): Unit = Cookies.set(
    global.encodeURIComponent(key).asInstanceOf[String],
    global.encodeURIComponent(value).asInstanceOf[String],
    expiration
  )

  def set(key: String, value: String, path: Uri): Unit = Cookies.set(
    global.encodeURIComponent(key).asInstanceOf[String],
    global.encodeURIComponent(value).asInstanceOf[String],
    path
  )

  def set(key: String, value: String, expiration: Instant, path: Uri): Unit = Cookies.set(
    global.encodeURIComponent(key).asInstanceOf[String],
    global.encodeURIComponent(value).asInstanceOf[String],
    expiration,
    path
  )

  def unset(key: String): Unit = Cookies.unset(global.encodeURIComponent(key).asInstanceOf[String])
}
