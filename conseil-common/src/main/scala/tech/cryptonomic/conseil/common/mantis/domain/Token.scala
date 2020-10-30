package tech.cryptonomic.conseil.common.mantis.domain

/**
  * Mantis token.
  */
case class Token(
    address: String,
    blockHash: String,
    blockNumber: String,
    name: String,
    symbol: String,
    decimals: String,
    totalSupply: String
)
