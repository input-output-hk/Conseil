package tech.cryptonomic.conseil.common.mantis.rpc.json

/**
  * Response from `eth_getTransactionByHash` Mantis JSON-RPC api call.
  * More info at: https://eth.wiki/json-rpc/API
  */
case class Transaction(
    hash: String,
    nonce: String,
    blockHash: String,
    blockNumber: String,
    transactionIndex: String,
    from: String,
    to: Option[String],
    value: String,
    gasPrice: String,
    gas: String,
    input: String,
    pending: Option[Boolean],
    isOutgoing: Option[Boolean]
)
