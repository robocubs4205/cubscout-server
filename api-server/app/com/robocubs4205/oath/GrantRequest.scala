package com.robocubs4205.oath

/**
  * Created by trevor on 9/16/17.
  */
sealed trait GrantRequest {
  def clientId:String
  def clientSecret:Option[String]
  def grantType:GrantType
}

object GrantRequest {
  case class AccessCodeGrantRequest(accessCode: String,
                                    clientId:String,
                                    clientSecret:Option[String]) extends GrantRequest {
    val grantType = GrantType.AuthCode
  }
  case class PasswordGrantRequest(username:String,
                                  password:String,
                                  clientId:String,
                                  clientSecret:Option[String]) extends GrantRequest {
    val grantType = GrantType.Password
  }
}
