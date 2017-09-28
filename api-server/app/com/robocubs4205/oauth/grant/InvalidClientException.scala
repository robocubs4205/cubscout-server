package com.robocubs4205.oauth.grant

case object InvalidClientException extends RuntimeException(s"Invalid client id or secret")
