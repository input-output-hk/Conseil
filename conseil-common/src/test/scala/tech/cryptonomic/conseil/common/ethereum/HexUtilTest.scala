package tech.cryptonomic.conseil.common.ethereum

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import tech.cryptonomic.conseil.common.util.HexUtil

class HexUtilTest extends AnyWordSpec with Matchers {
  "Utils" should {
      "decode hex string" in {
        HexUtil.hexToString("0x313233") shouldBe "123"
      }

      "convert hex string to big decimal" in {
        HexUtil.hexStringToBigDecimal("0x1") shouldBe 0x1
      }
    }
}
