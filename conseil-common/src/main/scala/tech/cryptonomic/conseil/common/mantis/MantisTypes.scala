package tech.cryptonomic.conseil.common.mantis

object MantisTypes {

  /** Case class representing hash to identify blocks across many block chains */
  final case class MantisBlockHash(value: String) extends AnyVal

}
