package tech.cryptonomic.conseil.common.mantis.rpc.json

/**
  * Response from `eth_getTransactionReceipt` Mantis JSON-RPC api call.
  * More info at: https://eth.wiki/json-rpc/API
  */
case class TransactionReceipt(
    transactionHash: String,
    transactionIndex: String,
    blockNumber: String,
    blockHash: String,
    cumulativeGasUsed: String,
    gasUsed: String,
    contractAddress: Option[String],
    logs: Seq[Log]
)
