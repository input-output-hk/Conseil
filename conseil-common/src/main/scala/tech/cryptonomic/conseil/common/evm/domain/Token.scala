package tech.cryptonomic.conseil.common.evm.domain

case class Token(
    address: String,
    blockHash: String,
    blockNumber: String,
    name: String,
    symbol: String,
    decimals: String,
    totalSupply: String
)
