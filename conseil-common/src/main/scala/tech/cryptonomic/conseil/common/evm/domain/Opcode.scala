package tech.cryptonomic.conseil.common.evm.domain

case class Opcode(offset: Int, instruction: Instruction, parameters: BigInt)
