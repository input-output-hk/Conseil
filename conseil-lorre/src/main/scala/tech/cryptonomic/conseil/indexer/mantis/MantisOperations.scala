package tech.cryptonomic.conseil.indexer.mantis

import cats.effect.{Concurrent, Resource}
import com.typesafe.scalalogging.LazyLogging
import fs2.Stream
import slickeffect.Transactor

import tech.cryptonomic.conseil.common.config.Platforms.MantisBatchFetchConfiguration
import tech.cryptonomic.conseil.common.rpc.RpcClient
import tech.cryptonomic.conseil.common.mantis.MantisPersistence
import tech.cryptonomic.conseil.common.mantis.rpc.MantisClient
import tech.cryptonomic.conseil.indexer.config.{Custom, Depth, Everything, Newest}
import tech.cryptonomic.conseil.common.mantis.domain.{Bytecode, Contract}

/**
  * Mantis operations for Lorre.
  *
  * @param mantisClient JSON-RPC client to communicate with the Mantis node
  * @param persistence DB persistence methods for the Mantis blockchain
  * @param tx [[slickeffect.Transactor]] to perform a Slick operations on the database
  * @param batchConf Configuration containing batch fetch values
  */
class MantisOperations[F[_]: Concurrent](
    mantisClient: MantisClient[F],
    persistence: MantisPersistence[F],
    tx: Transactor[F],
    batchConf: MantisBatchFetchConfiguration
) extends LazyLogging {

  /**
    * Start Lorre with mode defined with [[Depth]].
    *
    * @param depth Can be: Newest, Everything or Custom
    */
  def loadBlocksAndLogs(depth: Depth): Stream[F, Unit] =
    Stream
      .eval(tx.transact(persistence.getLatestIndexedBlock))
      .zip(mantisClient.getMostRecentBlockNumber.map(Integer.decode))
      .flatMap {
        case (latestIndexedBlock, mostRecentBlockNumber) =>
          val range = depth match {
            case Newest => latestIndexedBlock.map(_.number + 1).getOrElse(0) to mostRecentBlockNumber
            case Everything => 0 to mostRecentBlockNumber
            case Custom(depth) if depth > mostRecentBlockNumber && latestIndexedBlock.isEmpty =>
              1 to mostRecentBlockNumber
            case Custom(depth) if depth > mostRecentBlockNumber && latestIndexedBlock.nonEmpty =>
              latestIndexedBlock.map(_.number + 1).getOrElse(0) to mostRecentBlockNumber
            case Custom(depth) => (mostRecentBlockNumber - depth) to mostRecentBlockNumber
          }

          loadBlocksWithTransactions(range) ++ extractTokens(range)
      }

  /**
    * Get blocks from Mantis node through Mantis client and save them into the database using Slick.
    * In the beginning, the current list of blocks is obtained from the database and removed from the computation.
    *
    * @param range Inclusive range of the block's height
    */
  def loadBlocksWithTransactions(range: Range.Inclusive): Stream[F, Unit] =
    Stream
      .eval(tx.transact(persistence.getIndexedBlockHeights(range)))
      .flatMap(
        existingBlocks =>
          Stream
            .range(range.start, range.end)
            .filter(height => !existingBlocks.contains(height))
            .map(n => s"0x${n.toHexString}")
            .through(mantisClient.getBlockByNumber(batchConf.blocksBatchSize))
            .flatMap {
              case block if block.transactions.size > 0 =>
                Stream
                  .emit(block)
                  .through(mantisClient.getTransactions(batchConf.transactionsBatchSize))
                  .chunkN(Integer.MAX_VALUE)
                  .map(txs => (block, txs.toList))
              case block => Stream.emit((block, Nil))
            }
            .flatMap {
              case (block, txs) if block.transactions.size > 0 =>
                Stream
                  .emits(txs)
                  .through(mantisClient.getTransactionReceipt)
                  .chunkN(Integer.MAX_VALUE)
                  .map(receipts => (block, txs, receipts.toList))
              case (block, txs) => Stream.emit((block, Nil, Nil))
            }
            .evalTap { // log every 10 block
              case (block, txs, receipts) if Integer.decode(block.number) % 10 == 0 =>
                Concurrent[F].delay(
                  logger.info(
                    s"Save block with height: ${block.number} txs: ${txs.size} logs: ${receipts.map(_.logs.size).sum}"
                  )
                )
              case _ => Concurrent[F].unit
            }
            .evalTap {
              case (block, txs, receipts) =>
                tx.transact(persistence.createBlock(block, txs, receipts))
            }
            .flatMap {
              case (block, txs, receipts) =>
                Stream
                  .emits(receipts.filter(_.contractAddress.isDefined))
                  .through(mantisClient.getContract(batchConf.contractsBatchSize))
                  .chunkN(Integer.MAX_VALUE)
                  .evalTap(contracts => tx.transact(persistence.createContracts(contracts.toList)))
            }
      )
      .drain

  /**
    * Get tokens created in the given block number range.
    *
    * @param range Inclusive range of the block's height
    */
  def extractTokens(range: Range.Inclusive): Stream[F, Unit] =
    Stream
      .eval(tx.transact(persistence.getContracts(range)))
      .flatMap(Stream.emits)
      .map(
        row =>
          Contract(
            address = row.address,
            blockHash = row.blockHash,
            blockNumber = s"0x${row.blockNumber.toHexString}",
            isErc20 = row.isErc20,
            isErc721 = row.isErc721,
            bytecode = Bytecode(row.bytecode)
          )
      )
      .through(mantisClient.getTokenInfo)
      .evalTap(token => Concurrent[F].delay(logger.info(s"Save token: ${token.name}")))
      .chunkN(batchConf.tokensBatchSize)
      .evalTap(tokens => tx.transact(persistence.createTokens(tokens.toList)))
      .drain

}

object MantisOperations {

  /**
    * Create [[cats.Resource]] with [[MantisOperations]].
    *
    * @param rpcClient JSON-RPC client to communicate with the Mantis node
    * @param tx [[slickeffect.Transactor]] to perform a Slick operations on the database
    * @param batchConf Configuration containing batch fetch values
    */
  def resource[F[_]: Concurrent](
      rpcClient: RpcClient[F],
      tx: Transactor[F],
      batchConf: MantisBatchFetchConfiguration
  ): Resource[F, MantisOperations[F]] =
    for {
      mantisClient <- MantisClient.resource(rpcClient)
      persistence <- MantisPersistence.resource
    } yield new MantisOperations[F](mantisClient, persistence, tx, batchConf)
}
