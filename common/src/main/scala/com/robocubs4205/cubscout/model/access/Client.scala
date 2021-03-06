package com.robocubs4205.cubscout.model.access

import com.netaporter.uri.Uri
import com.robocubs4205.cubscout.TokenVal

sealed trait Client {
  def id: TokenVal

  def name: String

  def author: String

  def redirectUris: Seq[Uri]

  def firstParty: Boolean
}

object Client {

  sealed trait ClientWithSecret extends Client {
    def secret: TokenVal
  }

  case class ServerClient(id: TokenVal,
                          name: String,
                          author: String,
                          secret: TokenVal,
                          redirectUris: Seq[Uri],
                          firstParty:Boolean = false) extends ClientWithSecret

  case class BrowserClient(id: TokenVal,
                           name: String,
                           author: String,
                           redirectUris: Seq[Uri],
                           firstParty:Boolean = false) extends Client

  case class NativeClient(id: TokenVal,
                          name: String,
                          author: String,
                          redirectUris: Seq[Uri],
                          firstParty:Boolean = false) extends Client

}