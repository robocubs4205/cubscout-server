package com.robocubs4205.cubscout.access.model

import java.time.Instant
import java.util.UUID

import com.robocubs4205.cubscout.TokenVal

import scala.util.Try

trait AccessToken {
  def selector: TokenVal

  def validator: TokenVal

  def created: Instant

  def toTokenString = Seq(selector.toString, validator.toString).mkString("_")
}

object AccessToken {

  case class AccessTokenWithRefreshToken(
    selector: TokenVal,
    validator: TokenVal,
    refreshTokenSelector: TokenVal,
    created: Instant = Instant.now()
  ) extends AccessToken

  case class StandaloneAccessToken(
    selector:TokenVal,
    validator:TokenVal,
    clientId:TokenVal,
    userId:TokenVal,
    scopes: Set[Scope],
    created:Instant = Instant.now()
  ) extends AccessToken

  case class AccessTokenRep(selector: TokenVal, validator: TokenVal)

  def parse(string: String):Try[AccessTokenRep] = for {
    parts <- Try(string.split('_'))
    selector <- TokenVal(parts(0)) if parts.length == 2
    validator <- TokenVal(parts(1))
  } yield AccessTokenRep(selector, validator)
}