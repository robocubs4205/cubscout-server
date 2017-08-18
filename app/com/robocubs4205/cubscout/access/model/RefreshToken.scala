package com.robocubs4205.cubscout.access.model

import com.robocubs4205.cubscout.TokenVal

import scala.util.Try

/**
  * Created by trevor on 8/7/17.
  */
case class RefreshToken (selector:TokenVal,validator:TokenVal,clientId:TokenVal,userId:TokenVal,scopes:Seq[Scope]) {
  def toTokenString = Seq(selector.toString,validator.toString).mkString("_")
}

object RefreshToken {
  case class RefreshTokenRep(selector:TokenVal,validator:TokenVal)

  def parse(string:String): Try[RefreshTokenRep] = for{
    parts <- Try(string.split('_'))
    selector <- TokenVal(parts(0)) if parts.length==2
    validator <- TokenVal(parts(1))
  } yield RefreshTokenRep(selector,validator)

  def tupled = (apply _).tupled
}
