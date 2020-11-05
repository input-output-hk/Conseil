package tech.cryptonomic.conseil.common.util

import scala.util.Try

object HexUtil {

  def hexToString(value: String): String =
    value.stripPrefix("0x").grouped(2).toArray.map(Integer.parseInt(_, 16).toChar).mkString.trim

  def hexStringToBigDecimal(value: String): BigDecimal =
    BigDecimal(Try(BigInt(value.stripPrefix("0x"), 16)).getOrElse(BigInt(0)))

}
