package com.robocubs4205.oauth

import java.time.Instant

/**
  * Created by trevor on 9/16/17.
  */
case class Grant(accessToken:String,
                 refreshToken:Option[String],
                 scopes:Seq[String],
                 expires:Option[Instant])
