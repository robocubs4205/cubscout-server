package com.robocubs4205.cubscout.model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Match(id:Long, eventId:Long, number:Long, `type`:String)

object Match{
  implicit val matchFormat:Format[Match] = (
    (JsPath \ "id").formatWithDefault[Long](0) and
      (JsPath \ "eventId").format[Long] and
      (JsPath \ "number").format[Long] and
      (JsPath \ "matchType").format[String]
  )(Match.apply,unlift(Match.unapply))

  def tupled = (apply _).tupled
}
