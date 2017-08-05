package com.robocubs4205

import java.time.{LocalDate, Year}

import play.api.libs.json._

import scala.collection.Seq
import scala.util.Try

/**
  * Created by trevor on 7/22/17.
  */
package object cubscout {
  implicit val localDateWrites: Writes[LocalDate] = date => JsString(date.toString)
  implicit val localDateReads: Reads[LocalDate] = { value: JsValue =>
    value.validate[String].flatMap(str =>
      Try(LocalDate.parse(str)).map(JsSuccess(_)).recover {
        case _ => JsError(Seq(JsPath -> Seq(JsonValidationError("error.invalidDate"))))
      }.get
    )
  }
  implicit val yearWrites: Writes[Year] = year => JsNumber(year.getValue)
  implicit val yearReads: Reads[Year] = { value: JsValue =>
    value.validate[Int].flatMap(i =>
      Try(Year.of(i)).map(JsSuccess(_)).recover {
        case _ => JsError(Seq(JsPath -> Seq(JsonValidationError("error.invalidYear"))))
      }.get
    )
  }
}
