package com.robocubs4205.oath

import java.time.Instant

/**
  * Created by trevor on 9/16/17.
  */
case class Grant(accessToken:String,
                 refreshToken:Option[String],
                 scopes:Seq[String],
                 expiresIn:Option[Instant])
