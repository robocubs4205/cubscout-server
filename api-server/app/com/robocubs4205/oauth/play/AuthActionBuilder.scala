package com.robocubs4205.oauth.play

import com.netaporter.uri.Uri
import com.robocubs4205.oauth.InvalidRequestException
import play.api.libs.json.Json
import play.api.mvc._
import Results._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class AuthActionBuilder[B](bodyParser:BodyParser[B])(implicit ec:ExecutionContext)
  extends ActionBuilder[AuthRequest,B]{
  override def parser = bodyParser

  override def invokeBlock[A](request: Request[A], block: (AuthRequest[A]) => Future[Result]) = {
    authRequest(request).map(block).recover{
      case InvalidRequestException=>Future(InvalidRequestException).map(Json.toJson(_)).map(BadRequest(_))
      case t => Future.failed(t)
    }.get
  }

  override protected def executionContext = ec

  val RESPONSE_TYPE = "response_type"
  val CLIENT_ID = "client_id"
  val REDIRECT_URI = "redirect_uri"
  val SCOPE = "scope"
  val STATE = "state"

  def authRequest[A](request: Request[A]):Try[AuthRequest[A]] = {
    val parameters = request.queryString.map {case (k,v) => k->v.mkString}

    (for{
      responseType <- parameters.get(RESPONSE_TYPE)
      clientId <- parameters.get(CLIENT_ID)
      redirectUri <- parameters.get(REDIRECT_URI)
      scope <- parameters.get(SCOPE).map(_.split(" "))
    } yield {
      Try(Uri.parse(redirectUri)).map{ redirectUri=>
        AuthRequest(clientId ,redirectUri ,scope,request)
      }
    }).getOrElse(Failure(InvalidRequestException))
  }
}
