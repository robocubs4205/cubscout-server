package com.robocubs4205.oauth.grant

case object InvalidAuthCodeException
  extends RuntimeException("The authorization code was invalid or expired")
