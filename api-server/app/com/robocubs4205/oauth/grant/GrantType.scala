package com.robocubs4205.oauth.grant

/**
  * Created by trevor on 9/16/17.
  */
sealed trait GrantType

object GrantType{
  case object AuthCode extends GrantType
  case object Password extends GrantType
}
