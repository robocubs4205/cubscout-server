package com.robocubs4205.cubscout.model

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Team(id:Long,number:Long,name:Option[String],gameType:String,districtId:Option[Long])

object Team{
  implicit val teamFormat:Format[Team] = (
    (JsPath \ "id").formatWithDefault[Long](0) and
      (JsPath \ "number").format[Long] and
      (JsPath \ "name").formatNullable[String] and
      (JsPath \ "gameType").format[String] and
      (JsPath \ "districtId").formatNullable[Long]
  )(Team.apply,unlift(Team.unapply))

  def tupled = (apply _).tupled
}
