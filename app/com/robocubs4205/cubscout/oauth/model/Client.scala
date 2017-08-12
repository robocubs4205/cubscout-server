package com.robocubs4205.cubscout.oauth.model

import java.util.UUID

import com.netaporter.uri.Uri

sealed trait Client {
  def id: UUID

  def name: String

  def author: String

}

object Client {

  sealed trait ClientWithSecret extends Client {
    def secret: UUID
  }

  case class FirstPartyClient(id: UUID, name: String, secret: Option[UUID], redirectUris: Seq[Uri]) extends ClientWithSecret {
    val author = "Robocubs"
  }

  case class ServerClient(id: UUID, name: String, author: String, secret: UUID, redirectUris: Seq[Uri]) extends ClientWithSecret

  case class BrowserClient(id: UUID, name: String, author: String, redirectUris: Seq[Uri]) extends Client

  case class NativeClient(id: UUID, name: String, author: String, redirectUris: Seq[Uri]) extends Client

}