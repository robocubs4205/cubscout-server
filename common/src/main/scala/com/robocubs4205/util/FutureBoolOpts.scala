package com.robocubs4205.util

import scala.concurrent.{ExecutionContext, Future}

case class FutureBoolOpts(f:Future[Boolean])(implicit ec:ExecutionContext){
  def falseToFail[T](t:Throwable,v:T=()) = f.flatMap(if (_) Future(v) else Future.failed(t))
}
