package com.robocubs4205

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

package object util {
  implicit def optTryToTryOpt[T](v:Option[Try[T]]):Try[Option[T]] = v match {
    case Some(Success(v)) => Success(Some(v))
    case Some(Failure(t)) => Failure(t)
    case None => Success(None)
  }
  implicit def tryOptToOptTry[T](v:Try[Option[T]]) = v match {
    case Success(Some(v)) => Some(Success(v))
    case Success(None) => None
    case Failure(t) => Some(Failure(t))
  }

  implicit case class FutureBoolOpts(f:Future[Boolean])(implicit ec:ExecutionContext){
    def falseToFail[T](t:Throwable,v:T=()) = f.flatMap(if (_) Future(v) else Future.failed(t))
  }
}
