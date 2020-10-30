package tech.cryptonomic.conseil.common.EvmDomain

/**
  * Ethereum bytecode's opcode.
  */
case class Opcode(offset: Int, instruction: Instruction, parameters: BigInt)
