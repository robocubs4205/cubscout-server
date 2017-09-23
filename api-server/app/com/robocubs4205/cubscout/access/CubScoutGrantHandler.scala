package com.robocubs4205.cubscout.access

import java.time.Duration

import com.robocubs4205.cubscout.{CubScoutDb, TokenVal}
import com.robocubs4205.oath.{GrantRequest, GrantType}
import com.robocubs4205.oath.slick.SlickGrantHandlerBase

import scala.concurrent.ExecutionContext

/**
  * Created by trevor on 9/16/17.
  */
class CubScoutGrantHandler(csdb:CubScoutDb)(implicit ec:ExecutionContext)
  extends SlickGrantHandlerBase(csdb.dbConfig){
  import csdb._
  import dbConfig._
  import profile.api._
  override def validateClient(id: String): dbConfig.profile.api.DBIO[Boolean] =
  TokenVal(id).map { id =>
    for{
      serverClient <- serverClients.filter(_.id === id).result.headOption
      browserClient <- browserClients.filter(_.id === id).result.headOption
      nativeClient <- nativeClients.filter(_.id === id).result.headOption
    } yield serverClient.isDefined || browserClient.isDefined || nativeClient.isDefined
  } getOrElse DBIO.successful(false)

  override def authenticateClient(id: String, secret: String): dbConfig.profile.api.DBIO[Boolean] = ???

  override def clientIsAuthorizedForRequestType(id: String, grantType: GrantType): dbConfig.profile.api.DBIO[Boolean] = ???

  override def requestIsValidForAuthCode(request: GrantRequest.AccessCodeGrantRequest): dbConfig.profile.api.DBIO[Boolean] = ???

  override def authenticateUser(username: String, password: String): dbConfig.profile.api.DBIO[Boolean] = ???

  override def createRefreshToken(request: GrantRequest): dbConfig.profile.api.DBIO[String] = ???

  override def createAccessToken(request: GrantRequest, refreshToken: Option[String]): dbConfig.profile.api.DBIO[String] = ???

  override def scopesFromAuthCodeAndClient(authCode: String, clientId: String): dbConfig.profile.api.DBIO[Seq[String]] = ???

  override def accessCodeLifetime(request: GrantRequest): dbConfig.profile.api.DBIO[Option[Duration]] = ???

  override def scopesFromPasswordRequest(request: GrantRequest.PasswordGrantRequest): dbConfig.profile.api.DBIO[Seq[String]] = ???
}
