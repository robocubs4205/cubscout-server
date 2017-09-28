package com.robocubs4205.util

import java.time.{LocalDate, Year}

import com.netaporter.uri.Uri
import play.api.libs.json._

import scala.collection.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

import scala.language.implicitConversions

trait Util {
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
  implicit val UriWrites: Writes[Uri] = uri => JsString(uri.toString())
  implicit val UriReads:Reads[Uri] = {value:JsValue =>
    value.validate[String].flatMap(s =>
      Try(Uri.parse(s)).map(JsSuccess(_)).recover {
        case _ => JsError(Seq(JsPath -> Seq(JsonValidationError("error.invalidUri"))))
      }.get
    )
  }

  //noinspection VariablePatternShadow
  implicit def optTryToTryOpt[T](v:Option[Try[T]]):Try[Option[T]] = v match {
    case Some(Success(v)) => Success(Some(v))
    case Some(Failure(t)) => Failure(t)
    case None => Success(None)
  }
  //noinspection VariablePatternShadow
  implicit def tryOptToOptTry[T](v:Try[Option[T]]) = v match {
    case Success(Some(v)) => Some(Success(v))
    case Success(None) => None
    case Failure(t) => Some(Failure(t))
  }

  implicit class FutureBoolOpts(f:Future[Boolean])(implicit ec:ExecutionContext){
    def falseToFail[T](t:Throwable,v:T=()) = f.flatMap(if (_) Future(v) else Future.failed(t))
  }
}
