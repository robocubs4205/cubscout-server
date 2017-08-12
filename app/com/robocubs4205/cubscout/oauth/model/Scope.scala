package com.robocubs4205.cubscout.oauth.model

/**
  * A specific permission a client has
  * Unless otherwise specified, a client can only perform the actions allowed by a scope if the user of the client
  * is authorized to do so
  */
sealed trait Scope

object Scope {
}
