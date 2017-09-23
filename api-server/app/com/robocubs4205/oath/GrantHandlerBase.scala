package com.robocubs4205.oath

import java.time.{Duration, Instant}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Handles Grant requests by combining library-user-defined methods
  */
trait GrantHandlerBase extends GrantHandler {
  self: {
    val ec:ExecutionContext
  } =>
  implicit val e = ec
  override def handleRequest(request: GrantRequest): Future[Grant] = {
    def falseToFail[T](t: Throwable, v: T = ())(b: Boolean) =
      if (b) Future(v)
      else Future.failed(t)

    {
      request.clientSecret.map {
        secret =>
          authenticateClient(request.clientId, secret)
      } getOrElse validateClient(request.clientId)
    } flatMap falseToFail(UnauthorizedException) flatMap { _ =>
      clientIsAuthorizedForRequestType(request.clientId, request.grantType)
    } flatMap falseToFail(UnauthorizedException) flatMap { _ =>
      request match {
        case request: GrantRequest.AccessCodeGrantRequest =>
          requestIsValidForAuthCode(request) flatMap falseToFail(UnauthorizedException) flatMap { _ =>
            if (request.clientSecret.isDefined) for {
              refreshToken <- createRefreshToken(request)
              accessToken <- createAccessToken(request, Some(refreshToken))
              scopes <- scopesFromAuthCodeAndClient(request.accessCode, request.clientId)
              lifetime <- accessCodeLifetime(request)
            } yield Grant(
              accessToken,
              Some(refreshToken),
              scopes,
              lifetime.map(Instant.now().plus(_))
            )
            else for {
              accessToken <- createAccessToken(request, None)
              scopes <- scopesFromAuthCodeAndClient(request.accessCode, request.clientId)
              lifetime <- accessCodeLifetime(request)
            } yield Grant(accessToken, None, scopes, lifetime.map(Instant.now().plus(_)))
          }
        case request: GrantRequest.PasswordGrantRequest =>
          authenticateUser(request.username, request.password) flatMap falseToFail(UnauthorizedException) flatMap { _ =>
            if (request.clientSecret.isDefined) for {
              refreshToken <- createRefreshToken(request)
              accessToken <- createAccessToken(request, Some(refreshToken))
              scopes <- scopesFromPasswordRequest(request)
              lifetime <- accessCodeLifetime(request)
            } yield Grant(
              accessToken,
              Some(refreshToken),
              scopes,
              lifetime.map(Instant.now().plus(_))
            )
            else for {
              accessToken <- createAccessToken(request, None)
              scopes <- scopesFromPasswordRequest(request)
              lifetime <- accessCodeLifetime(request)
            } yield Grant(accessToken, None, scopes, lifetime.map(Instant.now().plus(_)))
          }
      }
    }
  }

  def validateClient(id: String): Future[Boolean]

  def authenticateClient(id: String, secret: String): Future[Boolean]

  def clientIsAuthorizedForRequestType(id: String, grantType: GrantType): Future[Boolean]

  /**
    * returns true if the auth code exists and the request is valid for the auth code. Returns false
    * otherwise. "Valid for the auth code" typically means that the client id matches the one that
    * the auth code was granted for.
    */
  def requestIsValidForAuthCode(request: GrantRequest.AccessCodeGrantRequest): Future[Boolean]

  def authenticateUser(username: String, password: String): Future[Boolean]

  def createRefreshToken(request: GrantRequest): Future[String]

  def createAccessToken(request: GrantRequest, refreshToken: Option[String]): Future[String]

  def scopesFromAuthCodeAndClient(authCode: String, clientId: String): Future[Seq[String]]

  def accessCodeLifetime(request: GrantRequest): Future[Option[Duration]]

  def scopesFromPasswordRequest(request: GrantRequest.PasswordGrantRequest): Future[Seq[String]]
}
