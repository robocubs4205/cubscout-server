package com.robocubs4205.cubscout.access

import java.util.Date
import javax.inject.Inject

import com.robocubs4205.cubscout.{CubScoutDb, TokenVal}
import com.robocubs4205.cubscout.model.access._

import scala.concurrent.{ExecutionContext, Future}
import scalaoauth2.provider.{AccessToken => ProviderAccessToken, RefreshToken => ProviderRefreshToken, _}
import com.github.t3hnar.bcrypt._
import com.robocubs4205.cubscout.model.access.AccessToken.{AccessTokenRep, AccessTokenWithRefreshToken, StandaloneAccessToken}
import com.robocubs4205.cubscout.model.access.Client.ClientWithSecret
import slick.dbio.DBIOAction
import com.netaporter.uri.Uri
import com.robocubs4205.cubscout.model.access.{Client, RefreshToken, Scope, User}
import play.api.Logger

import scala.util.{Failure, Success, Try}

/**
  * Handles the password grant, authorization code, and refresh token grant types
  */
class CubScoutDataHandler @Inject()(private[access] val csdb: CubScoutDb)(implicit ec: ExecutionContext) extends DataHandler[User] {

  import csdb._
  import dbConfig._
  import profile.api._

  override def findAuthInfoByAccessToken(accessToken: ProviderAccessToken): Future[Option[AuthInfo[User]]] =
    AccessToken.parse(accessToken.token).map { token =>
      db.run {
        for {
          standaloneAccessToken <- standaloneAccessTokens.filter(_.selector === token.selector).result.headOption
          accessTokenWithRefreshToken <- accessTokensWithRefreshTokens.filter(_.selector === token.selector).result
            .headOption
          authInfo <- standaloneAccessToken.map(authInfoFrom) orElse
            accessTokenWithRefreshToken.map(authInfoFrom) getOrElse DBIO.successful(None)
        } yield authInfo
      }
    }.getOrElse(Future(None))

  private[access] def authInfoFrom(standaloneAccessToken: StandaloneAccessToken): DBIO[Option[AuthInfo[User]]] =
    for {
      user <- users.filter(_.id === standaloneAccessToken.userId).result.headOption
      client <- clientWithId(standaloneAccessToken.clientId)
    } yield for {
      user <- user
      client <- client
    } yield AuthInfo(
      user,
      Some(client.id.toString),
      if (standaloneAccessToken.scopes.isEmpty) None else Some(Scope.toString(standaloneAccessToken.scopes)),
      client.redirectUris.headOption.map(_.toString)
    )

  private[access] def authInfoFrom(accessTokenWithRefreshToken: AccessTokenWithRefreshToken): DBIO[Option[AuthInfo[User]]] =
    for {
      refreshToken <- refreshTokens.filter(_.selector === accessTokenWithRefreshToken.refreshTokenSelector).result.headOption
      user <- refreshToken.map(refreshToken =>
        users.filter(_.id === refreshToken.userId).result.headOption
      ).getOrElse(DBIO.successful(None))
      client <- refreshToken.map(refreshToken =>
        clientWithId(refreshToken.clientId)
      ).getOrElse(DBIO.successful(None))
    } yield for {
      refreshToken <- refreshToken
      user <- user
      client <- client
    } yield AuthInfo(
      user,
      Some(client.id.toString),
      if (refreshToken.scopes.isEmpty) None else Some(Scope.toString(refreshToken.scopes)),
      client.redirectUris.headOption.map(_.toString)
    )

  private[this] def clientWithId(clientId: TokenVal): DBIO[Option[Client]] =
    for {
      serverClient <- serverClients.filter(_.id === clientId).result.headOption
      nativeClient <- nativeClients.filter(_.id === clientId).result.headOption
      browserClient <- browserClients.filter(_.id === clientId).result.headOption
    } yield serverClient.map(_.asInstanceOf[Client])
      .orElse(nativeClient.map(_.asInstanceOf[Client]))
      .orElse(browserClient.map(_.asInstanceOf[Client]))

  override def findAccessToken(token: String): Future[Option[ProviderAccessToken]] =
    AccessToken.parse(token).map {
      token =>
        db.run {
          for {
            accessTokenWithRefreshToken <- accessTokensWithRefreshTokens.filter(_.selector === token.selector).result
              .headOption
            standaloneAccessToken <- standaloneAccessTokens.filter(_.selector === token.selector).result.headOption
            providerAccessToken <- accessTokenWithRefreshToken.map(providerAccessTokenFrom) orElse
              standaloneAccessToken.map(providerAccessTokenFrom) getOrElse
              DBIO.successful(None)
          } yield providerAccessToken
        }
    }.getOrElse(Future(None))

  private[access] def providerAccessTokenFrom(accessTokenWithRefreshToken: AccessTokenWithRefreshToken): DBIO[Option[ProviderAccessToken]] =
    for {
      refreshToken <- refreshTokens.filter(_.selector === accessTokenWithRefreshToken.refreshTokenSelector).result
        .headOption
      providerAccessToken <- refreshToken.map(providerAccessTokenFrom(accessTokenWithRefreshToken, _))
        .getOrElse(DBIO.successful(None))
    } yield providerAccessToken

  private[access] def providerAccessTokenFrom(accessTokenWithRefreshToken: AccessTokenWithRefreshToken, refreshToken: RefreshToken): DBIO[Option[ProviderAccessToken]] =
    DBIO.successful(if (accessTokenWithRefreshToken.refreshTokenSelector != refreshToken.selector) None else
      Some(ProviderAccessToken(
        accessTokenWithRefreshToken.tokenString,
        Some(refreshToken.tokenString),
        if (refreshToken.scopes.isEmpty) None else Some(Scope.toString(refreshToken.scopes)),
        None,
        Date.from(accessTokenWithRefreshToken.created)
      )))

  private[access] def providerAccessTokenFrom(standaloneAccessToken: StandaloneAccessToken): DBIO[Option[ProviderAccessToken]] =
    DBIO.successful(Some(ProviderAccessToken(
      standaloneAccessToken.tokenString,
      None,
      if (standaloneAccessToken.scopes.isEmpty) None else Some(Scope.toString(standaloneAccessToken.scopes)),
      None,
      Date.from(standaloneAccessToken.created)
    )))

  override def validateClient(
                               maybeCredential: Option[ClientCredential],
                               request: AuthorizationRequest
                             ): Future[Boolean] = maybeCredential.fold(Future(false)) {
    credential =>
      db.run {
        TokenVal(credential.clientId).map {
          id =>
            narrowRequest(request) match {
              //only allowed for clients that can keep a secret
              case r: RefreshTokenRequest => {
                for {
                  secret <- credential.clientSecret
                  secret <- TokenVal(secret).toOption
                } yield for {
                  serverClient <- serverClients.filter(_.id === id).result.headOption
                } yield serverClient.isDefined && serverClient.get.secret == secret
              }.getOrElse(DBIO.successful(false))
              //only allowed for first party clients
              case _: PasswordRequest => {
                for {
                  secret <- credential.clientSecret
                  secret <- TokenVal(secret).toOption
                } yield clientWithId(id).map {
                  case Some(c) if c.firstParty => true
                  case _ => false
                }
              }.getOrElse(DBIO.successful(false))
              //only allowed for clients that can NOT keep a secret
              case r: AuthorizationCodeRequest => for {
                browserClient <- browserClients.filter(_.id === id).result.headOption
                nativeClient <- nativeClients.filter(_.id === id).result.headOption
              } yield (for {
                redirectUri <- r.redirectUri.map(Uri.parse(_))
                sameUri <- browserClient.map(_.redirectUris.contains(redirectUri)) orElse
                  nativeClient.map(_.redirectUris.contains(redirectUri))
                    .map(_ && r.param("code_challenge").isDefined)
              } yield sameUri) getOrElse false

              //other grant types not allowed
              case r =>
                Logger.info(s"invalid grant type ${r.grantType}")
                DBIO.successful(false)
            }
        }.recoverWith {
          case t: Throwable =>
            Logger.info("error when parsing client id", t)
            Failure(t)
        }.getOrElse(DBIO.successful(false))
      }
  }

  private[this] def narrowRequest(request: AuthorizationRequest): AuthorizationRequest = request.grantType match {
    case OAuthGrantType.PASSWORD => PasswordRequest(request)
    case OAuthGrantType.REFRESH_TOKEN => RefreshTokenRequest(request)
    case OAuthGrantType.AUTHORIZATION_CODE => AuthorizationCodeRequest(request)
  }

  override def findUser(
                         maybeCredential: Option[ClientCredential],
                         request: AuthorizationRequest
                       ): Future[Option[User]] = request match {
    case r: PasswordRequest => db.run {
      users.filter(_.username === r.username).result.headOption.map {
        case Some(user) =>
          r.password.isBcryptedSafe(user.hashedPassword).map(if (_) Some(user) else None).getOrElse(None)
        case None => None
      }
    }
  }

  override def createAccessToken(authInfo: AuthInfo[User]): Future[ProviderAccessToken] =
    db.run({
      for {
        clientId <- authInfo.clientId.fold[Try[TokenVal]](Failure(NoClientIdException))(TokenVal(_))
        scopes <- authInfo.scope.fold[Try[Set[Scope]]](Success(Set()))(Scope.parseSet)
        token = StandaloneAccessToken(TokenVal(), TokenVal(), clientId, authInfo.user.id, scopes)
      } yield for {
        _ <- standaloneAccessTokens += token
        providerAccessToken <- providerAccessTokenFrom(token)
      } yield providerAccessToken
    }.recover {
      case t: Throwable => DBIO.failed(t)
    }.get).map(_.get)

  override def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[ProviderAccessToken]] =
    db.run {
      val tokenWithRefresh = for {
        refreshToken <- refreshTokens.filter(_.userId === authInfo.user.id).result.headOption
        accessToken <- accessTokensWithRefreshTokens.filter(_.refreshTokenSelector === refreshToken.map(_.selector)).result.headOption
        providerAccessToken <- {
          for {
            refreshToken <- refreshToken
            accessToken <- accessToken
          } yield providerAccessTokenFrom(accessToken, refreshToken)
        }.getOrElse(DBIO.successful(None))
      } yield providerAccessToken

      val standaloneToken = for {
        accessToken <- standaloneAccessTokens.filter(_.userId === authInfo.user.id).result.headOption
        providerAccessToken <- accessToken.map(providerAccessTokenFrom).getOrElse(DBIO.successful(None))
      } yield providerAccessToken

      for {
        standaloneToken <- standaloneToken
        tokenWithRefresh <- tokenWithRefresh
      } yield standaloneToken.map(Some(_)) orElse tokenWithRefresh.map(Some(_)) getOrElse None
    }

  override def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[ProviderAccessToken] =
    Future.fromTry(RefreshToken.parse(refreshToken)).flatMap {
      token =>
        db.run {
          for {
            refreshToken <- refreshTokens.filter(_.selector === token.selector).result.headOption
            accessToken <- refreshToken.map {
              refreshToken =>
                (accessTokensWithRefreshTokens returning accessTokensWithRefreshTokens) +=
                  AccessTokenWithRefreshToken(
                    TokenVal(),
                    TokenVal(),
                    refreshToken.selector
                  )
            }.map(_.map(Some(_))).getOrElse(DBIO.successful(None))
            providerAccessToken <- {
              for {
                refreshToken <- refreshToken
                accessToken <- accessToken
              } yield providerAccessTokenFrom(accessToken, refreshToken)
            }.getOrElse(DBIO.successful(None))
          } yield providerAccessToken
        }
    }.flatMap {
      case None => Future.failed(InvalidRefreshTokenException)
      case Some(a) => Future(a)
    }

  override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] =
    AuthCode.parse(code).map(
      authCode => db.run {
        authCodes.filter(_.selector === authCode.selector).result.headOption.flatMap(_.map {
          authCode =>
            users.filter(_.id === authCode.userId).result.headOption.map(_.map(
              AuthInfo(_, Some(authCode.clientId.toString), None, Some(authCode.redirectUrl))))
        }.getOrElse(DBIO.successful(None)))
      }
    ).getOrElse(Future(None))

  override def deleteAuthCode(code: String): Future[Unit] =
    AuthCode.parse(code).map(
      authCode => db.run {
        authCodes.filter(_.selector === authCode.selector).delete
      }.map(_ => ())
    ).getOrElse(Future.successful(()))

  override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] =
    Future(RefreshToken.parse(refreshToken)).flatMap(
      _.map(
        token => db.run {
          (refreshTokens.filter(_.selector === token.selector) join users on (_.userId === _.id)).result.headOption
        }.map(_.map(tup => AuthInfo(tup._2, Some(tup._1.clientId.toString), None, None)))
      ).getOrElse(Future(None))
    )

  case object InvalidScopeException extends IllegalArgumentException("invalid scope or scopes")

  case object NoClientIdException extends IllegalArgumentException("No client id")

  case object InvalidRefreshTokenException extends IllegalArgumentException("the specified refresh token is invalid")

}
