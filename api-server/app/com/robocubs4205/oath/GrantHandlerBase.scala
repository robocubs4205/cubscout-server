package com.robocubs4205.oath

import com.robocubs4205.util._
import java.time.Instant

import com.netaporter.uri.Uri

import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

trait GrantHandlerBase[Client, User, Id, Secret, RefreshToken, AccessToken, AuthCode, Scope]
  extends GrantHandler {

  implicit def ec:ExecutionContext

  def parseId(s: String): Try[Id]

  def parseSecret(s: String): Try[Secret]

  def parseRefreshToken(s: String): Try[RefreshToken]

  def parseAuthCode(s: String): Try[AuthCode]

  def parseAccessToken(s: String): Try[AccessToken]

  def parseScope(s: String): Try[Scope]

  def writeRefreshToken(v: RefreshToken): String = v.toString

  def writeAccessToken(v: AccessToken): String = v.toString

  def WriteScope(v: Scope): String = v.toString

  def supportedGrantTypes: Seq[GrantType]

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

  private[this] def parseScopes(scopes: Seq[String]): Try[Seq[Scope]] = {
    scopes.map(parseScope).foldLeft(Try(Seq[Scope]())) {
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

  override def handleRequest(request: GrantRequest) = {
    (for {
      id <- parseId(request.clientId)
      secret <- optTryToTryOpt(request.clientSecret.map(parseSecret))
    } yield {
      (for {
        _ <- Future(supportedGrantTypes.contains(request.grantType)).falseToFail(UnsupportedGrantTypeException)
        client <- client(id)
        _ <- secret match {
          case None => Future(())
          case Some(secret) => authenticateClient(client, secret).map(_ => client)
        }
        _ <- uriRegisteredForClient(request.redirectUri, client).falseToFail(InvalidRedirectException)
        _ <- grantTypeAllowed(client, request.grantType).falseToFail(UnauthorizedGrantTypeException)
      } yield client).flatMap { client =>
        request match {
          case request: GrantRequest.AuthCodeGrantRequest => parseAuthCode(request.authCode).map { authCode =>
            for {
              _ <- authCodeValidForClientAndSecret(authCode, client, secret).falseToFail(InvalidAuthCodeException)
              scopes <- scopesFromAuthCode(authCode)
              user <- user(authCode)
            } yield (client, scopes, user)
          }.recover(PartialFunction(t => Future.failed(t))).get

          case request: GrantRequest.PasswordGrantRequest =>
            for {
              user <- user(request.username)
              _ <- authenticateUser(user, request.password).falseToFail(InvalidUserException)
              scopes <- parseScopes(request.scopes.split(" ")).map(Future(_))
                .recover(PartialFunction(t => Future.failed(t))).get
            } yield (client, scopes, user)
        }
      }.flatMap {
        case (client, scopes, user) =>
          for {
            _ <- scopesAllowedForClient(scopes, client).falseToFail(InvalidScopeException)
            refreshToken <- maybeCreateRefreshToken(user, client, scopes, request.grantType)
            (accessToken, expires) <- createAccessToken(user, client, scopes, request.grantType, refreshToken)
          } yield Grant(
            writeAccessToken(accessToken),
            refreshToken.map(writeRefreshToken),
            scopes.map(WriteScope),
            Some(expires))
      }
    }).recover(PartialFunction(_ => Future.failed(InvalidClientException))).get
  }
}
