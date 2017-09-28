package com.robocubs4205.oauth

case object InvalidAuthCodeException
  extends RuntimeException("The authorization code was invalid or expired")
