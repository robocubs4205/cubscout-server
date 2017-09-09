package com.robocubs4205.cubscout.access

import java.time.Instant

import com.github.t3hnar.bcrypt._
import com.netaporter.uri.Uri
import com.robocubs4205.cubscout.model.access.AccessToken.{AccessTokenWithRefreshToken, StandaloneAccessToken}
import com.robocubs4205.cubscout.model.access.Client.FirstPartyClient
import com.robocubs4205.cubscout.model.access.Scope.{ManageTeam, ScoreMatches}
import com.robocubs4205.cubscout.model.access._
import com.robocubs4205.cubscout.model.access.{AccessToken, Client, RefreshToken, User}
import com.robocubs4205.cubscout.{CubScoutDb, TokenVal}
import org.scalatest.Outcome
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.db.slick.{DatabaseConfigProvider, DbName, SlickApi}
import play.api.test.Injecting
import slick.basic.{BasicProfile, DatabaseConfig}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps

class CubScoutDataHandlerSpec extends PlaySpec with GuiceOneAppPerSuite with Injecting {

  implicit val ec: ExecutionContext = ExecutionContext.global

  val dbConfigProvider = new DatabaseConfigProvider {
    override def get[P <: BasicProfile]: DatabaseConfig[P] =
      inject[SlickApi].dbConfig[P](DbName("test"))
  }

  val csdb: CubScoutDb = new CubScoutDb(dbConfigProvider)

  val handler = new CubScoutDataHandler(csdb)

  import csdb._
  import dbConfig._
  import profile.api._

  "authInfoFrom" must {
    "return None" when {
      "given a standalone access token" when {
        "the database has no users or clients" in {
          val token = StandaloneAccessToken(
            TokenVal(0x1234, 0x5678),
            TokenVal(0x9abc, 0xdef0),
            TokenVal(0x13579, 0x2468),
            TokenVal(0xa1b2c3, 0x1121),
            Set(),
            Instant.EPOCH
          )
          Await.result(
            db.run {
              handler.authInfoFrom(token)
            },
            20 seconds
          ) mustBe None
        }
        "the database has a different user and no clients" in withUser {
          _ =>
            val token = StandaloneAccessToken(
              TokenVal(0x1234, 0x5678),
              TokenVal(0x9abc, 0xdef0),
              TokenVal(0x13579, 0x2468),
              TokenVal(0xa1b2c3, 0x1121),
              Set(),
              Instant.EPOCH
            )
            Await.result(
              db.run {
                handler.authInfoFrom(token)
              },
              20 seconds
            ) mustBe None
        }
        "the database has the user and no clients" in withUser {
          user =>
            val token = StandaloneAccessToken(
              TokenVal(0x1234, 0x5678),
              TokenVal(0x9abc, 0xdef0),
              TokenVal(0x13579, 0x2468),
              user.id,
              Set(),
              Instant.EPOCH
            )
            Await.result(
              db.run {
                handler.authInfoFrom(token)
              },
              20 seconds
            ) mustBe None
        }
        "the database has no users and a different client" in withClient {
          _ =>
            val token = StandaloneAccessToken(
              TokenVal(0x1234, 0x5678),
              TokenVal(0x9abc, 0xdef0),
              TokenVal(0x13579, 0x2468),
              TokenVal(0xa1b2c3, 0x1121),
              Set(),
              Instant.EPOCH
            )
            Await.result(
              db.run {
                handler.authInfoFrom(token)
              },
              20 seconds
            ) mustBe None
        }
        "the database has no users and the client" in withClient {
          client =>
            val token = StandaloneAccessToken(
              TokenVal(0x1234, 0x5678),
              TokenVal(0x9abc, 0xdef0),
              client.id,
              TokenVal(0xa1b2c3, 0x1121),
              Set(),
              Instant.EPOCH
            )
            Await.result(
              db.run {
                handler.authInfoFrom(token)
              },
              20 seconds
            ) mustBe None
        }
        "the database has a different user and a different client" in withUser {
          _ => withClient {
            _ =>
              val token = StandaloneAccessToken(
                TokenVal(0x1234, 0x5678),
                TokenVal(0x9abc, 0xdef0),
                TokenVal(0x13579, 0x2468),
                TokenVal(0xa1b2c3, 0x1121),
                Set(),
                Instant.EPOCH
              )
              Await.result(
                db.run {
                  handler.authInfoFrom(token)
                },
                20 seconds
              ) mustBe None
          }
        }
      }
      "given an access token with refresh token" when {
        "the database has no refresh tokens" in {
          val token = AccessTokenWithRefreshToken(
            TokenVal(0x1234, 0x5678),
            TokenVal(0x9abc, 0xdef0),
            TokenVal(0x1342, 0xcab),
            Instant.EPOCH
          )
          Await.result(
            db.run {
              handler.authInfoFrom(token)
            },
            20 seconds
          ) mustBe None
        }
        "the database has a different refresh token" in withRefreshToken {
          (_, _, _) =>
            val token = AccessTokenWithRefreshToken(
              TokenVal(0x1234, 0x5678),
              TokenVal(0x9abc, 0xdef0),
              TokenVal(0x1342, 0xcab),
              Instant.EPOCH
            )
            Await.result(
              db.run {
                handler.authInfoFrom(token)
              },
              20 seconds
            ) mustBe None
        }
      }
    }
    "return an authInfo with the user and the client" when {
      "given a standalone access token" when {
        "the database has the user and client" in withUser {
          user => withClient {
            client =>
              val token = StandaloneAccessToken(
                TokenVal(0x1234, 0x5678),
                TokenVal(0x9abc, 0xdef0),
                client.id,
                user.id,
                Set(),
                Instant.EPOCH
              )
              val maybeAuthInfo = Await.result(
                db.run {
                  handler.authInfoFrom(token)
                },
                20 seconds
              )
              maybeAuthInfo mustBe defined
              maybeAuthInfo.get.user mustBe user
              maybeAuthInfo.get.clientId mustBe defined
              maybeAuthInfo.get.clientId.get mustBe client.id.toString
              maybeAuthInfo.get.redirectUri mustBe defined
              client.redirectUris.map(_.toString) must contain(maybeAuthInfo.get.redirectUri.get)
          }
        }
      }
      "given an access token with refresh token" when {
        "the database has the refresh token" in withRefreshToken {
          (client, user, refreshToken) =>
            val token = AccessTokenWithRefreshToken(
              TokenVal(0x1234, 0x5678),
              TokenVal(0x9abc, 0xdef0),
              refreshToken.selector,
              Instant.EPOCH
            )
            val maybeAuthInfo = Await.result(
              db.run {
                handler.authInfoFrom(token)
              },
              20 seconds
            )
            maybeAuthInfo mustBe defined
            val authInfo = maybeAuthInfo.get
            authInfo.user mustBe user
            authInfo.clientId mustBe defined
            val clientId = authInfo.clientId.get
            clientId mustBe client.id.toString
            authInfo.redirectUri mustBe defined
            val redirectUri = authInfo.redirectUri.get
            client.redirectUris.map(_.toString) must contain(redirectUri)
        }
      }
    }
    "return an authInfo with no scope" when {
      "given a standalone access token" when {
        "the database has the user and client" in withUser {
          user => withClient {
            client =>
              val token = StandaloneAccessToken(
                TokenVal(0x1234, 0x5678),
                TokenVal(0x9abc, 0xdef0),
                client.id,
                user.id,
                Set(),
                Instant.EPOCH
              )
              val maybeAuthInfo = Await.result(
                db.run {
                  handler.authInfoFrom(token)
                },
                20 seconds
              )
              maybeAuthInfo mustBe defined
              maybeAuthInfo.get.scope mustBe empty
          }
        }
      }
      "given an access token with refresh token" when {
        "the database has the refresh token" when {
          "the refresh token has no scope" in withRefreshTokenWithNoScope {
            (_, _, refreshToken) =>
              val token = AccessTokenWithRefreshToken(
                TokenVal(0x1234, 0x5678),
                TokenVal(0x9abc, 0xdef0),
                refreshToken.selector,
                Instant.EPOCH
              )
              val maybeAuthInfo = Await.result(
                db.run {
                  handler.authInfoFrom(token)
                },
                20 seconds
              )
              maybeAuthInfo mustBe defined
              val authInfo = maybeAuthInfo.get
              authInfo.scope mustBe empty
          }
        }
      }
    }
    "return an authInfo with the scope" when {
      "given a standalone access token" when {
        "the token has one scope" in withUser {
          user => withClient {
            client =>
              val token = StandaloneAccessToken(
                TokenVal(0x1234, 0x5678),
                TokenVal(0x9abc, 0xdef0),
                client.id,
                user.id,
                Set(ManageTeam),
                Instant.EPOCH
              )
              val maybeAuthInfo = Await.result(
                db.run {
                  handler.authInfoFrom(token)
                },
                20 seconds
              )
              maybeAuthInfo mustBe defined
              maybeAuthInfo.get.scope mustBe defined
              val tryParse = Scope.parseSet(maybeAuthInfo.get.scope.get)
              tryParse.isSuccess mustBe true
              tryParse.get mustBe Set(ManageTeam)
          }
        }
      }
      "given an access token with refresh token" when {
        "the database has the refresh token" when {
          "the refresh token has one scope" in withRefreshTokenWithOneScope {
            (_, _, refreshToken) =>
              val token = AccessTokenWithRefreshToken(
                TokenVal(0x1234, 0x5678),
                TokenVal(0x9abc, 0xdef0),
                refreshToken.selector,
                Instant.EPOCH
              )
              val maybeAuthInfo = Await.result(
                db.run {
                  handler.authInfoFrom(token)
                },
                20 seconds
              )
              maybeAuthInfo mustBe defined
              val authInfo = maybeAuthInfo.get
              authInfo.scope mustBe defined
              val maybeScopes = Scope.parseSet(authInfo.scope.get)
              maybeScopes.isSuccess mustBe true
              maybeScopes.get mustBe refreshToken.scopes
          }
        }
      }
    }
  }

  "ProviderAccessTokenFrom" must {
    "return a token" when {
      "given a standalone access token" in {
        val token = StandaloneAccessToken(
          TokenVal(0x1234, 0x5678),
          TokenVal(0x9abc, 0xdef0),
          TokenVal(0x13579, 0x2468),
          TokenVal(0xa1b2c3, 0x1121),
          Set(),
          Instant.EPOCH
        )
        Await.result(
          db.run(handler.providerAccessTokenFrom(token)),
          20 seconds
        ) mustBe defined
      }
      "given an access token with a refresh token that exists" in withRefreshToken {
        (_, _, refreshToken) =>
          val token = AccessTokenWithRefreshToken(
            TokenVal(0x1234, 0x5678),
            TokenVal(0x9abc, 0xdef0),
            refreshToken.selector,
            Instant.EPOCH
          )
          Await.result(
            db.run(handler.providerAccessTokenFrom(token)),
            20 seconds
          ) mustBe defined
      }
    }
    "return nothing" when {
      "given an access token with a refresh token that does not exist" in {
        val token = AccessTokenWithRefreshToken(
          TokenVal(0x1234, 0x5678),
          TokenVal(0x9abc, 0xdef0),
          TokenVal(0x13579, 0x2468),
          Instant.EPOCH
        )
        Await.result(
          db.run(handler.providerAccessTokenFrom(token)),
          20 seconds
        ) mustBe empty
      }
      "given an access token and a refresh token that don't match" in withRefreshToken {
        (_, _, refreshToken) =>
          val token = AccessTokenWithRefreshToken(
            TokenVal(0x1234, 0x5678),
            TokenVal(0x9abc, 0xdef0),
            TokenVal(0x13579, 0x2468),
            Instant.EPOCH
          )
          Await.result(
            db.run(handler.providerAccessTokenFrom(token, refreshToken)),
            20 seconds
          ) mustBe empty
      }
    }
  }

  "findAccessToken" must {
    "return none" when {
      "the token string is invalid" in {
        Await.result(
          handler.findAccessToken("invalid"),
          20 seconds
        ) mustBe empty
      }
      "there are no access tokens" in {
        val token = StandaloneAccessToken(
          TokenVal(0x1234, 0x5678),
          TokenVal(0x9abc, 0xdef0),
          TokenVal(0x13579, 0x2468),
          TokenVal(0xa1b2c3, 0x1121),
          Set(),
          Instant.EPOCH
        )
        Await.result(
          handler.findAccessToken(token.tokenString),
          20 seconds
        ) mustBe empty
      }
      "there is only one access token, which does not match" in withAccessToken {
        _ =>
          val token = StandaloneAccessToken(
            TokenVal(0x1234, 0x5678),
            TokenVal(0x9abc, 0xdef0),
            TokenVal(0x13579, 0x2468),
            TokenVal(0xa1b2c3, 0x1121),
            Set(),
            Instant.EPOCH
          )
          Await.result(
            handler.findAccessToken(token.tokenString),
            20 seconds
          ) mustBe empty
      }
    }
    "return the token" when {
      "there is a standalone access token which matches the token string" in withStandaloneAccessToken {
        token =>
          val result = Await.result(
            handler.findAccessToken(token.tokenString),
            20 seconds
          )
          result mustBe defined
          result.get.token mustBe token.tokenString
          result.get.refreshToken mustBe empty
          result.get.createdAt.toInstant mustBe token.created
      }
      "there is an access token with refresh token which matches the token string" in withAccessTokenWithRefreshToken {
        (accessToken, refreshToken) =>
          val result = Await.result(
            handler.findAccessToken(accessToken.tokenString),
            20 seconds
          )
          result mustBe defined
          result.get.token mustBe accessToken.tokenString
          result.get.refreshToken mustBe defined
          result.get.refreshToken.get mustBe refreshToken.tokenString
          result.get.createdAt.toInstant mustBe accessToken.created
      }
    }
    "return a token with no scope" when {
      "there is a standalone access token with no scope which matches the token string" in withStandaloneAccessTokenWithNoScope {
        (token) =>
          val result = Await.result(
            handler.findAccessToken(token.tokenString),
            20 seconds
          )
          result mustBe defined
          result.get.scope mustBe empty
      }
      "there is an access token with refresh token with no scope which matches the token string" in withAccessTokenWithRefreshTokenWithNoScope {
        (token, _) =>
          val result = Await.result(
            handler.findAccessToken(token.tokenString),
            20 seconds
          )
          result mustBe defined
          result.get.scope mustBe empty
      }
    }
    "return a token with the given scope" when {
      "there is a standalone access token with a scope which matches the token string" in withStandaloneAccessTokenWithOneScope {
        (token) =>
          val result = Await.result(
            handler.findAccessToken(token.tokenString),
            20 seconds
          )
          result mustBe defined
          result.get.scope mustBe defined
          val triedScopes = Scope.parseSet(result.get.scope.get)
          triedScopes.isSuccess mustBe true
          val scopes = triedScopes.get
          token.scopes mustBe scopes
      }
      "there is an access token with refresh token with a scope which matches the token string" in withAccessTokenWithRefreshTokenWithOneScope {
        (token, refreshToken) =>
          val result = Await.result(
            handler.findAccessToken(token.tokenString),
            20 seconds
          )
          result mustBe defined
          result.get.scope mustBe defined
          val triedScopes = Scope.parseSet(result.get.scope.get)
          triedScopes.isSuccess mustBe true
          val scopes = triedScopes.get
          refreshToken.scopes mustBe scopes
      }
    }
  }


  def withAccessTokenWithRefreshToken[T](test: (AccessTokenWithRefreshToken, RefreshToken) => T): T =
    withAccessTokenWithRefreshTokenWithNoScope(test)

  def withAccessTokenWithRefreshTokenWithNoScope[T](test: (AccessTokenWithRefreshToken, RefreshToken) => T): T =
    withRefreshTokenWithNoScope {
      (_, _, refreshToken) =>
        val token = AccessTokenWithRefreshToken(
          TokenVal(0x13571113L, 0x2481632L),
          TokenVal(0x3691215L, 0x43234323L),
          refreshToken.selector,
          Instant.EPOCH
        )
        insertAccessToken(token)
        test(token, refreshToken)
    }

  def withAccessTokenWithRefreshTokenWithOneScope[T](test: (AccessTokenWithRefreshToken, RefreshToken) => T): T =
    withRefreshTokenWithOneScope {
      (_, _, refreshToken) =>
        val token = AccessTokenWithRefreshToken(
          TokenVal(0x13571113L, 0x2481632L),
          TokenVal(0x3691215L, 0x43234323L),
          refreshToken.selector,
          Instant.EPOCH
        )
        insertAccessToken(token)
        test(token, refreshToken)
    }

  def withStandaloneAccessToken[T](test: (StandaloneAccessToken) => T): T = withStandaloneAccessTokenWithNoScope(test)

  def withStandaloneAccessTokenWithNoScope[T](test: (StandaloneAccessToken) => T): T = withClient {
    client => withUser {
      user =>
        val token = StandaloneAccessToken(
          TokenVal(0x13571113L, 0x2481632L),
          TokenVal(0x3691215L, 0x43234323L),
          client.id, user.id,
          Set(),
          Instant.EPOCH
        )
        insertAccessToken(token)
        test(token)
    }
  }

  def withStandaloneAccessTokenWithOneScope[T](test: (StandaloneAccessToken) => T): T = withClient {
    client => withUser {
      user =>
        val token = StandaloneAccessToken(
          TokenVal(0x13571113L, 0x2481632L),
          TokenVal(0x3691215L, 0x43234323L),
          client.id, user.id,
          Set(ManageTeam),
          Instant.EPOCH
        )
        insertAccessToken(token)
        test(token)
    }
  }

  def withAccessToken[T](test: (AccessToken) => T): T = withAccessTokenWithRefreshToken {
    (token, _) => test(token)
  }

  def insertAccessToken(token: StandaloneAccessToken) = Await.result(
    db.run {
      standaloneAccessTokens += token
    },
    20 seconds
  )

  def insertAccessToken(token: AccessTokenWithRefreshToken) = Await.result(
    db.run {
      accessTokensWithRefreshTokens += token
    },
    20 seconds
  )

  def withRefreshTokenWithNoScope[T](test: (Client, User, RefreshToken) => T): T = withClient {
    client => withUser {
      user => test(client, user, insertRefreshToken(client, user))
    }
  }

  def withRefreshTokenWithOneScope[T](test: (Client, User, RefreshToken) => T): T = withClient {
    client => withUser {
      user => test(client, user, insertRefreshTokenWithOneScope(client, user))
    }
  }

  def insertRefreshTokenWithOneScope(client: Client, user: User) = Await.result(
    db.run {
      val token = RefreshToken(TokenVal(0x1234, 0x56789L), TokenVal(0x1234, 0x56789L), client.id, user.id, Set(ManageTeam))
      for {
        _ <- refreshTokens += token
      } yield token
    },
    20 seconds
  )

  def withRefreshTokenWithTwoScopes[T](test: (Client, User, RefreshToken) => T): T = withClient {
    client => withUser {
      user => test(client, user, insertRefreshTokenWithTwoScopes(client, user))
    }
  }

  def insertRefreshTokenWithTwoScopes(client: Client, user: User) = Await.result(
    db.run {
      val token = RefreshToken(TokenVal(0x1234, 0x56789L), TokenVal(0x1234, 0x56789L), client.id, user.id, Set(ManageTeam, ScoreMatches))
      for {
        _ <- refreshTokens += token
      } yield token
    },
    20 seconds
  )

  def withRefreshToken[T](test: (Client, User, RefreshToken) => T): T = withRefreshTokenWithNoScope(test)

  def insertRefreshToken(client: Client, user: User) = Await.result(
    db.run {
      val token = RefreshToken(TokenVal(0x1234, 0x56789L), TokenVal(0x1234, 0x56789L), client.id, user.id, Set())
      for {
        _ <- refreshTokens += token
      } yield token
    },
    20 seconds
  )

  def withUser[T](test: (User) => T): T = test(insertUser)

  def insertUser = Await.result(
    db.run {
      val user = User(TokenVal(0xabcdef, 0x123456789L), "bob", "password".bcrypt)
      for {
        _ <- users += user
      } yield user
    },
    20 seconds
  )

  def withFirstPartyClient[T](test: (FirstPartyClient) => T): T = test(insertFirstPartyClient)

  def withClient[T](test: (Client) => T): T = withFirstPartyClient(test)

  def insertFirstPartyClient = Await.result(
    db.run {
      val fpc = FirstPartyClient(TokenVal(0xabc123, 0x12345), "CubScout", None, Seq(Uri.parse("http://example.com")))
      for {
        _ <- firstPartyClients += fpc
      } yield fpc
    },
    20 seconds
  )

  def withCleanDb[T](test: () => T): T = {
    createSchemas
    try test()
    finally dropSchemas
  }

  def createSchemas = Await.result(
    db.run {
      users.schema.create andThen
        serverClients.schema.create andThen
        browserClients.schema.create andThen
        nativeClients.schema.create andThen
        firstPartyClients.schema.create andThen
        refreshTokens.schema.create andThen
        accessTokensWithRefreshTokens.schema.create andThen
        standaloneAccessTokens.schema.create andThen
        authCodes.schema.create
    },
    20 seconds
  )

  def dropSchemas = Await.result(
    db.run {
      authCodes.schema.drop andThen
        standaloneAccessTokens.schema.drop andThen
        accessTokensWithRefreshTokens.schema.drop andThen
        refreshTokens.schema.drop andThen
        firstPartyClients.schema.drop andThen
        nativeClients.schema.drop andThen
        browserClients.schema.drop andThen
        serverClients.schema.drop andThen
        users.schema.drop
    },
    20 seconds
  )

  override def withFixture(test: NoArgTest): Outcome = {
    withCleanDb(test)
  }

}
