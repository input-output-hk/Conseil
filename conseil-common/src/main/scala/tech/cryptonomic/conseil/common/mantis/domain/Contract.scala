package tech.cryptonomic.conseil.common.mantis.domain

/**
  * Mantis contract.
  */
case class Contract(
    address: String,
    blockHash: String,
    blockNumber: String,
    isErc20: Boolean = false,
    isErc721: Boolean = false,
    bytecode: Bytecode
)
