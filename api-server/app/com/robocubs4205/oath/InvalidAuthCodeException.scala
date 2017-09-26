package com.robocubs4205.oath

case object InvalidAuthCodeException extends RuntimeException("The authorization code was invalid or expired")
