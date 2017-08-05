package com.robocubs4205.cubscout.model

import java.time.Year

import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.robocubs4205.cubscout._

/**
  * Created by trevor on 7/20/17.
  */
case class District(id: Long, code: String, name: String,gameType:String,firstYear:Year,lastYear:Option[Year])

object District{
  implicit val districtFormat: Format[District] = (
    (JsPath \ "id").formatWithDefault[Long](0) and
      (JsPath \ "code").format[String] and
      (JsPath \ "name").format[String] and
      (JsPath \ "gameType").format[String] and
      (JsPath \ "firstYear").format[Year] and
      (JsPath \ "lastYear").formatNullable[Year]
    ) (District.apply, unlift(District.unapply))

  def tupled = (apply _).tupled
}