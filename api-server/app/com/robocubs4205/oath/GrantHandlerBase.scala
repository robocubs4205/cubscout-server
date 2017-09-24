package com.robocubs4205.oath


import java.time.Instant

import com.netaporter.uri.Uri

import scala.concurrent.Future
import scala.language.higherKinds
import scala.util.{Failure, Success, Try}
import scalaz.Monad

abstract class GrantHandlerBase[Client, User, Id, Secret, RefreshToken, AccessToken, AuthCode, Scope](
  idParser: String => Try[Id],
  secretParser: String => Try[Secret],
  refreshTokenParser: String => Try[RefreshToken],
  accessTokenParser: String => Try[AccessToken],
  authCodeParser: String => Try[AuthCode],
  scopeParser: String => Try[Scope],
  refreshTokenWriter: RefreshToken => String = _.toString,
  accessTokenWriter: RefreshToken => String = _.toString,
  authCodeTokenWriter: RefreshToken => String = _.toString,
  scopeWriter: Scope => String = _.toString
) extends GrantHandler {
  private[this] def optTryToTryOpt[A](v: Option[Try[A]]): Try[Option[A]] = v match {
    case Some(Success(v)) => Success(Some(v))
    case Some(Failure(t)) => Failure(t)
    case None => Success(None)
  }

  private[this] def parseScopes(scopes: Seq[String]): Try[Seq[Scope]] = {
    scopes.map(scopeParser(_)).foldLeft(Try(Seq[Scope]())) {
      (ts, t) =>
        ts match {
          case Success(scopes) => t match {
            case Success(scope) => Success(scopes :+ scope)
            case Failure(th) => Failure(th)
          }
          case v@Failure(_) => v
        }
    }
  }

  private[this] def futureFalseToFail[T](t: Throwable, v: T = ())(b: Boolean): Future[T] =
    if (b) Future(v) else Future.failed(t)

  override def handleRequest(request: GrantRequest) = {
    (for {
      id <- idParser(request.clientId)
      secret <- optTryToTryOpt {
        request.clientSecret.map(secretParser(_))
      }
    } yield {

      (for {
        client <- client(id)
        _ <- secret match {
          case None => Future(())
          case Some(secret) => authenticateClient(client, secret).map(_ => client)
        }
        _ <- uriRegisteredForClient(request.redirectUri, client) flatMap futureFalseToFail(UnauthorizedException)
        _ <- grantTypeAllowed(client, request.grantType) flatMap futureFalseToFail(UnauthorizedException)
      } yield client).flatMap { client =>
        request match {
          case request: GrantRequest.AuthCodeGrantRequest => authCodeParser(request.authCode).map { authCode =>
            for {
              _ <- authCodeValidForClientAndSecret(authCode, client, secret) flatMap
                futureFalseToFail(UnauthorizedException)
              scopes <- scopesFromAuthCode(authCode)
              user <- user(authCode)
            } yield (client, scopes, user)
          }.recover(PartialFunction(t => Future.failed(t))).get

          case request: GrantRequest.PasswordGrantRequest =>
            for {
              user <- user(request.username)
              _ <- authenticateUser(user, request.password) flatMap futureFalseToFail(UnauthorizedException)
              scopes <- parseScopes(request.scopes.split(" ")).map(Future(_))
                .recover(PartialFunction(t => Future.failed(t))).get
            } yield (client, scopes, user)
        }
      }.flatMap {
        case (client, scopes, user) =>
          for {
            _ <- scopesAllowedForClient(scopes, client).flatMap {
              case true => Future(())
              case false => Future.failed(UnauthorizedException)
            }
            refreshToken <- maybeCreateRefreshToken(user, client, scopes, request.grantType)
            (accessToken, expires) <- createAccessToken(user, client, scopes, request.grantType, refreshToken)
          } yield Grant(
            authCodeTokenWriter(accessToken),
            refreshToken.map(refreshTokenWriter(_)),
            scopes.map(scopeWriter(_)),
            Some(expires))
      }
    }).recover(PartialFunction(t => Future.failed(t))).get
  }

  def client(id: Id): Future[Client]

  def authenticateClient(client: Client, secret: Secret): Future[Boolean]

  def uriRegisteredForClient(uri: Uri, client: Client): Future[Boolean]

  def grantTypeAllowed(client: Client, grantType: GrantType): Future[Boolean]

  def authCodeValidForClientAndSecret(authCode: AuthCode, client: Client, secret: Option[Secret]): Future[Boolean]

  def scopesFromAuthCode(authCode: AuthCode): Future[Seq[Scope]]

  def scopesAllowedForClient(scopes: Seq[Scope], client: Client): Future[Boolean]

  def user(username: String): Future[User]

  def user(authCode: AuthCode): Future[User]

  def authenticateUser(user: User, password: String): Future[Boolean]

  def maybeCreateRefreshToken(user: User,
    client: Client,
    scopes: Seq[Scope],
    grantType: GrantType): Future[Option[RefreshToken]]

  def createAccessToken(
    user: User,
    client: Client,
    scopes: Seq[Scope],
    grantType: GrantType,
    refreshToken: Option[RefreshToken]): Future[(AccessToken, Instant)]
}
