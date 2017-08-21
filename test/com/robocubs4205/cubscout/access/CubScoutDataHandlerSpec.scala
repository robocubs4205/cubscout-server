package com.robocubs4205.cubscout.access

import java.time.Instant

import com.github.t3hnar.bcrypt._
import com.netaporter.uri.Uri
import com.robocubs4205.cubscout.access.model.AccessToken.{AccessTokenWithRefreshToken, StandaloneAccessToken}
import com.robocubs4205.cubscout.access.model.Client.FirstPartyClient
import com.robocubs4205.cubscout.access.model.Scope.{ManageTeam, ScoreMatches}
import com.robocubs4205.cubscout.access.model.{Client, RefreshToken, Scope, User}
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

  "authInfoFrom" when {
    "given a StandaloneAccessToken" when {
      "the database has no users or clients" must {
        "return None" in {
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
      "the database has a different user and no clients" must {
        "return None" in withUser {
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
      "the database has a the user and no clients" must {
        "return None" in withUser {
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
      }
      "the database has no users and a different client" must {
        "return None" in withClient {
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
      "the database has no users and the client" must {
        "return None" in withClient {
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
      }
      "the database had a different user and a different client" must {
        "return None" in withUser {
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
      "the database had the user and client" must {
        "return an authInfo with the user and the client" in withUser {
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
      "the database had the user and client" when{
        "the token had no scope" must {
          "return an authInfo with no scope" in withUser {
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
        "the token had one scope" must {
          "return an authInfo with the scope" in withUser {
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
      }
      "the token had two scopes" must {
        "return an authInfo with the scopes" in withUser {
          user => withClient {
            client =>
              val token = StandaloneAccessToken(
                TokenVal(0x1234, 0x5678),
                TokenVal(0x9abc, 0xdef0),
                client.id,
                user.id,
                Set(ManageTeam,ScoreMatches),
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
              tryParse.get mustBe Set(ManageTeam,ScoreMatches)
          }
        }
      }
    }
    "given an AccessTokenWithRefreshToken" when {
      "the database has no RefreshToken" must {
        "return None" in {
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
      "the database has a different RefreshToken" must {
        "return None" in withRefreshToken{
          (_,_,_) =>
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
      "the database has the RefreshToken" must {
        "return an AuthInfo with the refreshToken's user and client" in withRefreshToken{
          (client,user,refreshToken) =>
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
        "return an AuthInfo with no scope" when {
          "the refresh token has no scope" in withRefreshTokenWithNoScope {
            (_,_,refreshToken)=>
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
        "return an AuthInfo with one scope" when {
          "the refresh token has one scope" in withRefreshTokenWithOneScope {
            (_,_,refreshToken)=>
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
        "return an AuthInfo with two scope" when {
          "the refresh token has two scopes" in withRefreshTokenWithTwoScopes {
            (_,_,refreshToken)=>
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



  def withRefreshTokenWithNoScope[T](test:(Client,User,RefreshToken)=>T):T = withRefreshToken(test)

  def withRefreshTokenWithOneScope[T](test:(Client,User,RefreshToken) => T):T = withClient{
    client => withUser {
      user => test(client,user,insertRefreshTokenWithOneScope(client,user))
    }
  }

  def insertRefreshTokenWithOneScope(client:Client,user:User) = Await.result(
    db.run{
      val token = RefreshToken(TokenVal(0x1234, 0x56789L),TokenVal(0x1234, 0x56789L),client.id,user.id,Set(ManageTeam))
      for{
        _ <- refreshTokens += token
      } yield token
    },
    20 seconds
  )

  def withRefreshTokenWithTwoScopes[T](test:(Client,User,RefreshToken)=>T):T = withClient{
    client => withUser{
      user => test(client,user,insertRefreshTokenWithTwoScopes(client,user))
    }
  }

  def insertRefreshTokenWithTwoScopes(client:Client,user:User) = Await.result(
    db.run{
      val token = RefreshToken(TokenVal(0x1234, 0x56789L),TokenVal(0x1234, 0x56789L),client.id,user.id,Set(ManageTeam,ScoreMatches))
      for{
        _ <- refreshTokens += token
      } yield token
    },
    20 seconds
  )

  def withRefreshToken[T](test:(Client,User,RefreshToken)=>T):T = withClient{
    client => withUser{
      user => test(client,user,insertRefreshToken(client,user))
    }
  }

  def insertRefreshToken(client:Client,user: User) = Await.result(
    db.run{
      val token = RefreshToken(TokenVal(0x1234, 0x56789L),TokenVal(0x1234, 0x56789L),client.id,user.id,Set())
      for{
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

  def withClient[T](test: (Client) => T):T = withFirstPartyClient(test)

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
