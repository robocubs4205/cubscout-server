package com.robocubs4205.oauth

case object InvalidClientException extends RuntimeException(s"Invalid client id or secret")
