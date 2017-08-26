package com.robocubs4205.cubscout

import org.apache.commons.codec.digest.DigestUtils
import play.api.libs.json._

/**
  * Created by trevor on 7/28/17.
  */
trait EtagWriter[T] {
  def etag(t:T):String
}

case class DefaultEtagWriter[T]()(implicit w:Writes[T]) extends EtagWriter[T]{
  def etag(t:T):String = "\""+DigestUtils.md5Hex(Json.toJson(t).toString())+"\""
}

object EtagWriter {
  implicit def defaultEtagWriter[T](implicit w:Writes[T]):EtagWriter[T] = DefaultEtagWriter[T]
}

