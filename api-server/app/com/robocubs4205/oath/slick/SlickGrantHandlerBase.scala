package com.robocubs4205.oath.slick

import java.time.{Duration, Instant}

import com.robocubs4205.oath._

import scala.concurrent.{ExecutionContext, Future}
import _root_.slick.basic.DatabaseConfig
import _root_.slick.basic.BasicProfile

/**
  * Functions identically to OAuthHandlerBase, but library-user-defined functions are defined in
  * terms of Slick DBIOActions, which allows slick to perform additional optimizations
  * @see GrantHandlerBase
  */
abstract class SlickGrantHandlerBase(protected val dbConfig: DatabaseConfig[_ <: BasicProfile])
                                    (implicit ec:ExecutionContext) extends GrantHandler {

  import dbConfig._
  import profile.api._

  override def handleRequest(request: GrantRequest): Future[Grant] = {
    def falseToFail[T](t: Throwable, v: T = ())(b: Boolean):DBIO[T] =
      if (b) DBIO.successful(v)
      else DBIO.failed(t)

    db.run {
      {
        request.clientSecret.map {
          secret =>
            authenticateClient(request.clientId, secret)
        } getOrElse validateClient(request.clientId)
      } flatMap falseToFail(UnauthorizedException) flatMap { _ =>
        clientIsAuthorizedForRequestType(request.clientId, request.grantType)
      } flatMap falseToFail(UnauthorizedException) flatMap[Grant,NoStream,Nothing] { _ =>
        request match {
          case request: GrantRequest.AccessCodeGrantRequest =>
            requestIsValidForAuthCode(request) flatMap
              falseToFail(UnauthorizedException) flatMap[Grant,NoStream,Nothing] { _ =>
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
            authenticateUser(request.username, request.password) flatMap
              falseToFail(UnauthorizedException) flatMap[Grant,NoStream,Nothing] { _ =>
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
  }

  def x = validateClient("") flatMap (_ => validateClient(""))

  def validateClient(id: String): DBIO[Boolean]

  def authenticateClient(id: String, secret: String): DBIO[Boolean]

  def clientIsAuthorizedForRequestType(id: String, grantType: GrantType): DBIO[Boolean]

  def requestIsValidForAuthCode(request: GrantRequest.AccessCodeGrantRequest): DBIO[Boolean]

  def authenticateUser(username: String, password: String): DBIO[Boolean]

  def createRefreshToken(request: GrantRequest): DBIO[String]

  def createAccessToken(request: GrantRequest, refreshToken: Option[String]): DBIO[String]

  def scopesFromAuthCodeAndClient(authCode: String, clientId: String): DBIO[Seq[String]]

  def accessCodeLifetime(request: GrantRequest): DBIO[Option[Duration]]

  def scopesFromPasswordRequest(request: GrantRequest.PasswordGrantRequest): DBIO[Seq[String]]
}
