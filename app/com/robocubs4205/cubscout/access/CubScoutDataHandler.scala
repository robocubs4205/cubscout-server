package com.robocubs4205.cubscout.access

import java.util.Date
import javax.inject.Inject

import com.robocubs4205.cubscout.{CubScoutDb, TokenVal}
import com.robocubs4205.cubscout.access.model._

import scala.concurrent.{ExecutionContext, Future}
import scalaoauth2.provider.{AccessToken => ProviderAccessToken, RefreshToken => ProviderRefreshToken, _}
import com.github.t3hnar.bcrypt._
import com.robocubs4205.cubscout.access.model.AccessToken.{AccessTokenRep, AccessTokenWithRefreshToken, StandaloneAccessToken}

/**
  * Created by trevor on 8/7/17.
  */
class CubScoutDataHandler @Inject()(csdb: CubScoutDb)(implicit ec: ExecutionContext) extends DataHandler[User] {

  import csdb._
  import dbConfig._
  import profile.api._

  override def findAuthInfoByAccessToken(accessToken: ProviderAccessToken): Future[Option[AuthInfo[User]]] =
    AccessToken.parse(accessToken.token).map {
      token => db.run {
        {
          for {
            standaloneAccessToken <- standaloneAccessTokens.filter(_.selector === token.selector).result.headOption
            accessTokenWithRefreshToken <- accessTokensWithRefreshTokens.filter(_.selector === token.selector).result.headOption
          } yield standaloneAccessToken.map(authInfoFrom) orElse
            accessTokenWithRefreshToken.map(authInfoFrom) getOrElse DBIO.successful(None)
        }.flatten
      }
    }.getOrElse(Future(None))

  private[this] def authInfoFrom(standaloneAccessToken: StandaloneAccessToken): DBIO[Option[AuthInfo[User]]] =
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

  private[this] def authInfoFrom(accessTokenWithRefreshToken: AccessTokenWithRefreshToken): DBIO[Option[AuthInfo[User]]] =
    for {
      refreshToken <- refreshTokens.filter(_.selector === accessTokenWithRefreshToken.refreshTokenSelector).result.head
      user <- users.filter(_.id === refreshToken.userId).result.headOption
      client <- clientWithId(refreshToken.clientId)
    } yield for {
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
      firstPartyClient <- firstPartyClients.filter(_.id === clientId).result.headOption
      serverClient <- serverClients.filter(_.id === clientId).result.headOption
      nativeClient <- nativeClients.filter(_.id === clientId).result.headOption
      browserClient <- browserClients.filter(_.id === clientId).result.headOption
    } yield firstPartyClient.map(_.asInstanceOf[Client])
      .orElse(serverClient.map(_.asInstanceOf[Client]))
      .orElse(nativeClient.map(_.asInstanceOf[Client]))
      .orElse(browserClient.map(_.asInstanceOf[Client]))

  override def findAccessToken(token: String): Future[Option[ProviderAccessToken]] =
    AccessToken.parse(token).map {
      token => db.run {
        {
          for {
            accessTokenWithRefreshToken <- accessTokensWithRefreshTokens.filter(_.selector === token.selector).result.headOption
            standaloneAccessToken <- standaloneAccessTokens.filter(_.selector === token.selector).result.headOption
          } yield accessTokenWithRefreshToken.map(ProviderAccessTokenFrom) orElse
            standaloneAccessToken.map(ProviderAccessTokenFrom) getOrElse
            DBIO.successful(None)
        }.flatten
      }
    }.recover {
      case t: Throwable => Future.failed(t)
    }.get

  private[this] def ProviderAccessTokenFrom(accessTokenWithRefreshToken: AccessTokenWithRefreshToken): DBIO[Option[ProviderAccessToken]] =
    for {
      refreshToken <- refreshTokens.filter(_.selector === accessTokenWithRefreshToken.refreshTokenSelector).result.headOption
    } yield refreshToken.map {
      refreshToken => ProviderAccessToken(
        accessTokenWithRefreshToken.toTokenString,
        Some(refreshToken.toTokenString),
        if (refreshToken.scopes.isEmpty) None else Some(Scope.toString(refreshToken.scopes)),
        None,
        Date.from(accessTokenWithRefreshToken.created)
      )
    }

  private[this] def ProviderAccessTokenFrom(standaloneAccessToken: StandaloneAccessToken): DBIO[Option[ProviderAccessToken]] =
    DBIO.successful(Some(ProviderAccessToken(
      standaloneAccessToken.toTokenString,
      None,
      if (standaloneAccessToken.scopes.isEmpty) None else Some(Scope.toString(standaloneAccessToken.scopes)),
      None,
      Date.from(standaloneAccessToken.created)
    )))

  override def validateClient(
    maybeCredential: Option[ClientCredential],
    request: AuthorizationRequest
  ): Future[Boolean] = maybeCredential.fold(Future(false)) {
    credential => db.run {
      TokenVal(credential.clientId).map {
        id => request match {
          case _: RefreshTokenRequest =>
            credential.clientSecret.fold[DBIO[Boolean]](DBIO.successful(false)) {
              secret => TokenVal(secret).map {
                secret =>
                  //secret comparisons deferred to after retrieval from database to prevent timing attacks
                  for {
                    serverClient <- serverClients.filter(_.id === id).result.headOption
                    firstPartyClient <- firstPartyClients.filter(_.id === id).filter(_.secret.isDefined)
                      .result.headOption
                  } yield (serverClient.isDefined && serverClient.get.secret == secret) ||
                    (firstPartyClient.isDefined && firstPartyClient.get.secret.get == secret)
              }.getOrElse(DBIO.successful(false))
            }
          case _: PasswordRequest =>
            if (credential.clientSecret.isDefined) TokenVal(credential.clientSecret.get)
              .map {
                secret => for {
                  firstPartyClient <- firstPartyClients.filter(_.id === id).filter(_.secret.isDefined)
                    .result.headOption
                } yield firstPartyClient.isDefined &&
                  firstPartyClient.get.secret.get == secret
              }.getOrElse(DBIO.successful(false))
            else for {
              firstPartyClient <- firstPartyClients.filter(_.id === id).filter(_.secret.isEmpty).result.headOption
            } yield firstPartyClient.isDefined
          case r: AuthorizationCodeRequest => for {
            browserClient <- browserClients.filter(_.id === id).result.headOption
            nativeApp <- nativeClients.filter(_.id === id).result.headOption
            firstPartyClient <- firstPartyClients.filter(_.id === id).result.headOption
          } yield browserClient.isDefined || (nativeApp.isDefined && r.param("code_challenge").isDefined) ||
            firstPartyClient.isDefined
          case _ => DBIO.successful(false)
        }
      }.getOrElse(DBIO.successful(false))
    }
  }.recover {
    case _ => false
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
    authInfo.clientId.map(TokenVal(_).map {
      clientId => authInfo.scope match {
        case Some(scope) => Scope.parseSeq(scope).map {
          scopes => db.run {
            val token = StandaloneAccessToken(TokenVal(), TokenVal(), clientId, authInfo.user.id, scopes)
            for {
              _ <- standaloneAccessTokens += token
            } yield token
          }.map(token => ProviderAccessToken(token.toTokenString, None, authInfo.scope, None, Date.from(token.created)))
        }.recover {
          case t: Throwable => Future.failed(t)
        }.get
        case None => db.run {
          val token = StandaloneAccessToken(TokenVal(), TokenVal(), clientId, authInfo.user.id, Seq())
          for {
            _ <- standaloneAccessTokens += token
          } yield token
        }.map(token => ProviderAccessToken(token.toTokenString, None, authInfo.scope, None, Date.from(token.created)))
      }
    }.recover {
      case t: Throwable => Future.failed(t)
    }.get).getOrElse(Future.failed(new IllegalArgumentException("No client id")))


  override def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[ProviderAccessToken]] =
    db.run {
      val tokenWithRefresh = for {
        refreshToken <- refreshTokens.filter(_.userId === authInfo.user.id).result.headOption
        accessToken <- accessTokensWithRefreshTokens.filter(_.refreshTokenSelector === refreshToken.get.selector).result.headOption if refreshToken.isDefined
      } yield for {
        refreshToken <- refreshToken
        accessToken <- accessToken
      } yield ProviderAccessToken(
        accessToken.toTokenString,
        Some(refreshToken.toTokenString),
        None,
        None,
        Date.from(accessToken.created)
      )

      val standaloneToken = for {
        accessToken <- standaloneAccessTokens.filter(_.userId === authInfo.user.id).result.headOption
      } yield for {
        accessToken <- accessToken
      } yield ProviderAccessToken(
        accessToken.toTokenString,
        None,
        None,
        None,
        Date.from(accessToken.created)
      )

      for {
        standaloneToken <- standaloneToken
        tokenWithRefresh <- tokenWithRefresh
      } yield standaloneToken.map(Some(_)) orElse tokenWithRefresh.map(Some(_)) getOrElse None
    }


  override def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[ProviderAccessToken] =
    Future(RefreshToken.parse(refreshToken)).flatMap(
      _.map(
        token => db.run({
          for {
            refreshToken <- refreshTokens.filter(_.selector === token.selector).result.headOption
            accessToken <- (accessTokensWithRefreshTokens returning accessTokensWithRefreshTokens) +=
              AccessTokenWithRefreshToken(
                TokenVal(),
                TokenVal(),
                refreshToken.get.selector
              )
          } yield ProviderAccessToken(
            accessToken.toTokenString,
            Some(refreshToken.get.toTokenString),
            None,
            None,
            Date.from(accessToken.created)
          )
        })
      ).get
    )

  override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] =
    AuthCode.parse(code).map(
      authCode => db.run {
        authCodes.filter(_.selector === authCode.selector).result.headOption.flatMap(_.map {
          authCode => users.filter(_.id === authCode.userId).result.headOption.map(_.map(
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
}
