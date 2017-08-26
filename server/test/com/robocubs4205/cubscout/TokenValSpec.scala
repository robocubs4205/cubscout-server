package com.robocubs4205.cubscout

import org.scalatestplus.play.PlaySpec

class TokenValSpec extends PlaySpec {
  "A TokenVal" must {
    "return a 4 digit string" in {
      val tokenVal = TokenVal(0x32ea,0x587d)
      val string = tokenVal.toString
      string must have length 22
    }

    "be the same after being converted to string and back" in {
      val tokenVal = TokenVal(0x32f0,0x587d)
      val string = tokenVal.toString
      val newTokenVal = TokenVal(string)
      newTokenVal.get mustBe tokenVal
    }
  }
}
