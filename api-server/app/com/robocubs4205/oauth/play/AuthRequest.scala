package com.robocubs4205.oauth.play

import com.netaporter.uri.Uri
import play.api.mvc.{Request, WrappedRequest}

case class AuthRequest[+A](
  clientId:String,
  redirectURI:Uri,
  scopes:Seq[String],
  request:Request[A]
) extends WrappedRequest[A](request)