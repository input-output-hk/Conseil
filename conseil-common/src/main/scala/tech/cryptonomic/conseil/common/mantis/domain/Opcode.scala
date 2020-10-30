package tech.cryptonomic.conseil.common.mantis.domain

/**
  * Mantis bytecode's opcode.
  */
case class Opcode(offset: Int, instruction: Instruction, parameters: BigInt)
