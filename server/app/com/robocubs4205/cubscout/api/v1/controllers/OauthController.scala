package com.robocubs4205.cubscout.api.v1.controllers

import javax.inject.Inject

import com.robocubs4205.cubscout.access.CubScoutDataHandler
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}

import scala.concurrent.ExecutionContext
import scalaoauth2.provider.{AccessToken => ProviderAccessToken, _}
import OAuthGrantType._


class OauthController @Inject()(cc:ControllerComponents, csDataHandler: CubScoutDataHandler)(implicit ec:ExecutionContext) extends AbstractController(cc)
  with OAuth2Provider{

  object MyTokenEndpoint extends TokenEndpoint {
    override val handlers = Map(
      AUTHORIZATION_CODE -> new AuthorizationCode(),
      REFRESH_TOKEN -> new RefreshToken(),
      PASSWORD -> new Password()
    )
  }

  override val tokenEndpoint = MyTokenEndpoint

  def accessToken = Action.async {implicit request:Request[AnyContent]=>
    issueAccessToken(csDataHandler)
  }
}
