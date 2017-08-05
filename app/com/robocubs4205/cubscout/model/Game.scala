package com.robocubs4205.cubscout.model

import java.time.Year

import com.robocubs4205.cubscout._

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Game(id:Long, name:String, `type`:String, year:Year)

object Game{
  implicit val gameFormat:Format[Game] = (
    (JsPath \ "id").formatWithDefault[Long](0) and
      (JsPath \ "name").format[String] and
      (JsPath \ "gameType").format[String] and
      (JsPath \ "year").format[Year]
  )(Game.apply,unlift(Game.unapply))

  def tupled = (apply _).tupled
}