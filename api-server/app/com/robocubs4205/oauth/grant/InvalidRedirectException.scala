package com.robocubs4205.oauth.grant

case object InvalidRedirectException
  extends RuntimeException("The redirect URI was unregistered or did not match the URI for the authorization code")
