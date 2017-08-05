package com.robocubs4205.cubscout.model

import java.time.LocalDate

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
  * Created by trevor on 7/21/17.
  */
case class Event(id: Long, districtId: Option[Long], gameId:Long, name: String, address: Option[String],
                 startDate: Option[LocalDate],endDate: Option[LocalDate])

object Event {
  implicit val eventFormat: Format[Event] = (
    (JsPath \ "id").formatWithDefault[Long](0) and
      (JsPath \ "districtId").formatNullable[Long] and
      (JsPath \ "gameId").format[Long] and
      (JsPath \ "name").format[String] and
      (JsPath \ "address").formatNullable[String] and
      (JsPath \ "startDate").formatNullable[LocalDate] and
      (JsPath \ "endDate").formatNullable[LocalDate]
    ) (Event.apply, unlift(Event.unapply))

  def tupled = (apply _).tupled
}