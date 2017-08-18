package com.robocubs4205.cubscout

import java.nio.ByteBuffer
import java.security.SecureRandom

import org.apache.commons.codec.binary.Base64

import scala.util.Try

case class TokenVal(val1:Long,val2:Long){
  override def toString: String = {
    val bytes: ByteBuffer = ByteBuffer.allocate(16)
    bytes.putLong(val1)
    bytes.putLong(val2)
    Base64.encodeBase64URLSafeString(bytes.array())
  }
}

object TokenVal {
  val csprng = new SecureRandom()
  def apply():TokenVal = apply(csprng.nextLong(),csprng.nextLong())
  def apply(str:String):Try[TokenVal] = Try{
    val bytes = ByteBuffer.wrap(Base64.decodeBase64(str))
    bytes.rewind()
    val val1 = bytes.getLong()
    val val2 = bytes.getLong()
    apply(val1,val2)
  }
}
