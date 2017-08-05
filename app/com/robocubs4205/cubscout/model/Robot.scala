package com.robocubs4205.cubscout.model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Robot(id:Long,teamId:Long,gameId:Long,name:Option[String])

object Robot{
  implicit val robotFormat:Format[Robot] = (
    (JsPath \ "id").formatWithDefault[Long](0) and
      (JsPath \ "teamId").format[Long] and
      (JsPath \ "gameId").format[Long] and
      (JsPath \ "name").formatNullable[String]
  )(Robot.apply,unlift(Robot.unapply))

  def tupled = (apply _).tupled
}
