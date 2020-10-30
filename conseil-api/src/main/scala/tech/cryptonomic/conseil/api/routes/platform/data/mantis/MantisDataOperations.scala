package tech.cryptonomic.conseil.api.routes.platform.data.mantis

import slick.jdbc.PostgresProfile.api._
import tech.cryptonomic.conseil.api.routes.platform.data.ApiDataOperations
import tech.cryptonomic.conseil.common.mantis.MantisTypes.MantisBlockHash
import tech.cryptonomic.conseil.common.generic.chain.DataTypes.{Query, QueryResponse}
import tech.cryptonomic.conseil.common.util.DatabaseUtil

import scala.concurrent.{ExecutionContext, Future}

/**
  * Contains list of available methods for fetching the data for Mantis-related block-chains
  *
  * @param prefix the name of the schema under which database are stored
  */
class MantisDataOperations(prefix: String) extends ApiDataOperations {
  override lazy val dbReadHandle: Database = DatabaseUtil.conseilDb

  /** Fetches the list of blocks for given query */
  def fetchBlocks(query: Query)(implicit ex: ExecutionContext): Future[Option[List[QueryResponse]]] =
    queryWithPredicates(prefix, "blocks", query).map(Option(_))

  /** Fetches the latest block, ordered by time */
  def fetchBlocksHead()(implicit ex: ExecutionContext): Future[Option[QueryResponse]] = {
    val filter = MantisFilter(limit = Some(1), sortBy = Some("timestamp"))
    queryWithPredicates(prefix, "blocks", filter.toQuery).map(_.headOption)
  }

  /** Fetches the block by specific hash */
  def fetchBlockByHash(hash: MantisBlockHash)(implicit ex: ExecutionContext): Future[Option[QueryResponse]] = {
    val filter = MantisFilter(blockIds = Set(hash.value))
    queryWithPredicates(prefix, "blocks", filter.toQuery).map(_.headOption)
  }

  /** Fetches the list of transactions for given query */
  def fetchTransactions(query: Query)(implicit ex: ExecutionContext): Future[Option[List[QueryResponse]]] =
    queryWithPredicates(prefix, "transactions", query).map(Some(_))

  /** Fetches the transaction by specific id (different from hash) */
  def fetchTransactionByHash(hash: String)(implicit ex: ExecutionContext): Future[Option[QueryResponse]] = {
    val filter = MantisFilter(transactionHashes = Set(hash))
    queryWithPredicates(prefix, "transactions", filter.toQuery).map(_.headOption)
  }

  /** Fetches the list of logs for given query */
  def fetchLogs(query: Query)(implicit ec: ExecutionContext): Future[Option[List[QueryResponse]]] =
    queryWithPredicates(prefix, "logs", query).map(Some(_))

  /** Fetches the list of reecipts for given query */
  def fetchReceipts(query: Query)(implicit ec: ExecutionContext): Future[Option[List[QueryResponse]]] =
    queryWithPredicates(prefix, "receipts", query).map(Some(_))

  /** Fetches the list of contracts for given query */
  def fetchContracts(query: Query)(implicit ec: ExecutionContext): Future[Option[List[QueryResponse]]] =
    queryWithPredicates(prefix, "contracts", query).map(Some(_))

  /** Fetches the list of tokens for given query */
  def fetchTokens(query: Query)(implicit ec: ExecutionContext): Future[Option[List[QueryResponse]]] =
    queryWithPredicates(prefix, "tokens", query).map(Some(_))

  /** Fetches the list of token transfers for given query */
  def fetchTokenTransfers(query: Query)(implicit ec: ExecutionContext): Future[Option[List[QueryResponse]]] =
    queryWithPredicates(prefix, "token_transfers", query).map(Some(_))

  /** Fetches the list of accounts for given query */
  def fetchAccounts(query: Query)(implicit ec: ExecutionContext): Future[Option[List[QueryResponse]]] =
    queryWithPredicates(prefix, "accounts", query).map(Some(_))

  /** Fetches the list of accounts for given query */
  def fetchAccountByAddress(address: String)(implicit ec: ExecutionContext): Future[Option[QueryResponse]] = {
    val filter = MantisFilter(accountAddresses = Set(address))
    queryWithPredicates(prefix, "accounts", filter.toQuery).map(_.headOption)
  }

}
