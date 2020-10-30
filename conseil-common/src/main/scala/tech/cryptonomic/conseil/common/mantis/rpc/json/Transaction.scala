package tech.cryptonomic.conseil.common.mantis.rpc.json

/**
  * Response from `eth_getTransactionByHash` Mantis JSON-RPC api call.
  * More info at: https://eth.wiki/json-rpc/API
  */
case class Transaction(
    blockHash: String,
    blockNumber: String,
    from: String,
    gas: String,
    gasPrice: String,
    hash: String,
    input: String,
    nonce: String,
    to: Option[String],
    transactionIndex: String,
    value: String,
    v: String,
    r: String,
    s: String
)
