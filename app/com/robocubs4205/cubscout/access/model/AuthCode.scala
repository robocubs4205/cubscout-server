package com.robocubs4205.cubscout.access.model

import com.robocubs4205.cubscout.TokenVal

import scala.util.Try

case class AuthCode (selector:TokenVal,validator:TokenVal,clientId:TokenVal,userId:TokenVal,redirectUrl:String){
  def toTokenString = Seq(selector.toString,validator.toString).mkString("_")
}

object AuthCode{
  case class AuthCodeRep(selector:TokenVal,validator:TokenVal)

  def parse(string:String):Try[AuthCodeRep] = for{
    parts <- Try(string.split('_'))
    selector <- TokenVal(parts(1)) if parts.length==2
    validator <- TokenVal(parts(2))
  } yield AuthCodeRep(selector,validator)

  def tupled = (apply _).tupled
}
