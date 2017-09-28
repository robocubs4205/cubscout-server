package com.robocubs4205.oauth

import scala.concurrent.Future

/**
  * Created by trevor on 9/16/17.
  */
trait GrantHandler {
  /**
    * Grants access for a request or fails
    */
  def handleRequest(request: GrantRequest): Future[Grant]
}
