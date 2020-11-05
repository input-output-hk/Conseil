package tech.cryptonomic.conseil.common.mantis.rpc

import io.circe.{Decoder, Encoder, HCursor, Json}
import tech.cryptonomic.conseil.common.evm.domain.Bytecode
import tech.cryptonomic.conseil.common.rpc.RpcClient.RpcRequest

/**
  * Mantis JSON-RPC api methods according to the specification at https://eth.wiki/json-rpc/API
  * These are selected methods necessary for the Lorre to work.
  */
object MantisRpcCommands {

  /**
    * Sealed trait to keep the list of the RPC methods only in this file.
    */
  sealed trait MantisRpcMethod

  /**
    * `eth_blockNumber` Mantis JSON-RPC api method.
    * Returns the number of most recent block.
    */
  object EthBlockNumber extends MantisRpcMethod {
    val rpcMethod = "eth_blockNumber"
    case object Params
    def request = RpcRequest("2.0", rpcMethod, Params, s"$rpcMethod")

    implicit val encodeParams: Encoder[Params.type] = (_) => Json.arr()
  }

  /**
    * `eth_getBlockByNumber` Mantis JSON-RPC api method.
    * If verbosity is true it returns the full transaction objects,
    * if false only the hashes of the transactions.
    * We only use verbosity=false in Lorre.
    */
  object EthGetBlockByNumber extends MantisRpcMethod {
    val rpcMethod = "eth_getBlockByNumber"
    case class Params(number: String, verbosity: Boolean)
    def request(number: String) = RpcRequest("2.0", rpcMethod, Params(number, false), s"${rpcMethod}_$number")

    implicit val encodeParams: Encoder[Params] = (params: Params) =>
      Json.arr(
        Json.fromString(params.number),
        Json.fromBoolean(params.verbosity)
      )
  }

  /**
    * `eth_getTransactionByHash` Mantis JSON-RPC api method.
    * Returns the information about a transaction requested by transaction hash.
    */
  object EthGetTransactionByHash extends MantisRpcMethod {
    val rpcMethod = "eth_getTransactionByHash"
    case class Params(hash: String)
    def request(hash: String) = RpcRequest("2.0", rpcMethod, Params(hash), s"${rpcMethod}_$hash")

    implicit val encodeParams: Encoder[Params] = (params: Params) =>
      Json.arr(
        Json.fromString(params.hash)
      )
  }

  /**
    * `eth_getTransactionReceipt` Mantis JSON-RPC api method.
    * Returns the transaction receipt requested.
    */
  object EthGetTransactionReceipt extends MantisRpcMethod {
    val rpcMethod = "eth_getTransactionReceipt"
    case class Params(txHash: String)
    def request(txHash: String) = RpcRequest("2.0", rpcMethod, Params(txHash), s"${rpcMethod}_$txHash")

    implicit val encodeParams: Encoder[Params] = (params: Params) =>
      Json.arr(
        Json.fromString(params.txHash)
      )
  }

  /**
    * `eth_getCode` Mantis JSON-RPC api method.
    * Returns code at a given address.
    */
  object EthGetCode extends MantisRpcMethod {
    val rpcMethod = "eth_getCode"
    case class Params(address: String, blockNumber: String)
    def request(address: String, blockNumber: String) =
      RpcRequest("2.0", rpcMethod, Params(address, blockNumber), s"${rpcMethod}_$address")

    implicit val encodeParams: Encoder[Params] = (params: Params) =>
      Json.arr(
        Json.fromString(params.address),
        Json.fromString(params.blockNumber)
      )
  }

  /**
    * `eth_call` Mantis JSON-RPC api method.
    * Executes a new message call immediately without creating a transaction on the block chain.
    */
  object EthCall extends MantisRpcMethod {
    val rpcMethod = "eth_call"
    case class Params(
        blockNumber: String,
        from: String,
        data: String
    )
    def request(blockNumber: String, from: String, data: String) =
      RpcRequest("2.0", rpcMethod, Params(blockNumber, from, data), s"${rpcMethod}_$from")

    implicit val encodeParams: Encoder[Params] = (params: Params) =>
      Json.arr(
        Json.obj(
          "to" -> Json.fromString(params.from),
          "data" -> Json.fromString(params.data)
        ),
        Json.fromString(params.blockNumber)
      )
  }

  // Decoders for the Mantis domain case classes.
  implicit val decodeBytecode: Decoder[Bytecode] = (c: HCursor) =>
    for {
      bytecode <- c.as[String]
    } yield Bytecode(bytecode)

}
