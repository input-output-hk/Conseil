package tech.cryptonomic.conseil.indexer.mantis

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

import cats.effect.{IO, Resource}
import com.typesafe.scalalogging.LazyLogging
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.Retry
import slick.jdbc.PostgresProfile.api._
import slickeffect.Transactor
import slickeffect.transactor.{config => transactorConfig}

import tech.cryptonomic.conseil.common.rpc.RpcClient
import tech.cryptonomic.conseil.common.util.DatabaseUtil
import tech.cryptonomic.conseil.common.config.Platforms
import tech.cryptonomic.conseil.common.config.Platforms.MantisConfiguration
import tech.cryptonomic.conseil.indexer.LorreIndexer
import tech.cryptonomic.conseil.indexer.config.LorreConfiguration
import tech.cryptonomic.conseil.indexer.logging.LorreProgressLogging

/**
  * Class responsible for indexing data for Mantis Blockchain.
  *
  * @param lorreConf Lorre configuration
  * @param mantisConf Mantis configuration
  */
class MantisIndexer(
    lorreConf: LorreConfiguration,
    mantisConf: MantisConfiguration
) extends LazyLogging
    with LorreIndexer
    with LorreProgressLogging {

  /**
    * Executor for the rpc client, timer and to handle stop method.
    */
  private val indexerExecutor = Executors.newFixedThreadPool(mantisConf.batching.indexerThreadsCount)

  /**
    * Dedicated executor for the http4s.
    */
  private val httpExecutor = Executors.newFixedThreadPool(mantisConf.batching.httpFetchThreadsCount)

  /**
    * [[cats.ContextShift]] is the equivalent to [[ExecutionContext]],
    * it's used by the Cats Effect related methods.
    */
  implicit private val contextShift = IO.contextShift(ExecutionContext.fromExecutor(indexerExecutor))

  /**
    * [[ExecutionContext]] for the Lorre indexer.
    */
  private val indexerEC = ExecutionContext.fromExecutor(indexerExecutor)

  /**
    * The timer to schedule continuous indexer runs.
    */
  implicit private val timer = IO.timer(indexerEC)

  /**
    * Dedicated [[ExecutionContext]] for the http4s.
    */
  private val httpEC = ExecutionContext.fromExecutor(httpExecutor)

  override def platform: Platforms.BlockchainPlatform = Platforms.Mantis

  // TODO: Handle the cancelation in the right way, now it's imposible to use `ctrl-C`
  //       to stop the mainLoop.
  override def start(): Unit = {

    /**
      * Repeat [[cats.IO]] after the specified interval.
      *
      * @param interval finite duration interval
      * @param f [[cats.IO]] to repeat
      */
    def repeatEvery[A](interval: FiniteDuration)(operations: IO[A]): IO[Unit] =
      for {
        _ <- operations
        _ <- IO.sleep(interval)
        _ <- repeatEvery(interval)(operations)
      } yield ()

    indexer
      .use(
        mantisOperations =>
          repeatEvery(lorreConf.sleepInterval) {

            /**
              * Place with all the computations for the Mantis.
              * Currently, it only contains the blocks. But it can be extended to
              * handle multiple computations.
              */
            IO.delay(logger.info(s"Start Lorre for Mantis")) *>
              mantisOperations.loadBlocksAndLogs(lorreConf.depth).compile.drain
          }
      )
      .unsafeRunSync()
  }

  override def stop(): Future[LorreIndexer.ShutdownComplete] =
    IO.delay {
      indexerExecutor.shutdown()
      httpExecutor.shutdown()
      LorreIndexer.ShutdownComplete
    }.unsafeToFuture

  /**
    * Lorre indexer for the Mantis. This method creates all the dependencies and wraps it into the [[cats.Resource]].
    */
  private def indexer: Resource[IO, MantisOperations[IO]] =
    for {
      httpClient <- BlazeClientBuilder[IO](httpEC).resource

      rpcClient <- RpcClient.resource(
        mantisConf.node.toString,
        maxConcurrent = mantisConf.batching.indexerThreadsCount,
        Retry(RpcClient.exponentialRetryPolicy(mantisConf.retry.maxWait, mantisConf.retry.maxRetry))(httpClient)
      )

      tx <- Transactor
        .fromDatabase[IO](IO.delay(DatabaseUtil.lorreDb))
        .map(_.configure(transactorConfig.transactionally)) // run operations in transaction

      mantisOperations <- MantisOperations.resource(rpcClient, tx, mantisConf.batching)
    } yield mantisOperations
}

object MantisIndexer {

  /**
    * Creates the Indexer which is dedicated for Mantis blockchain.
    *
    * @param lorreConf Lorre configuration
    * @param mantisConf Mantis configuration
    */
  def fromConfig(
      lorreConf: LorreConfiguration,
      mantisConf: MantisConfiguration
  ): LorreIndexer =
    new MantisIndexer(lorreConf, mantisConf)
}
