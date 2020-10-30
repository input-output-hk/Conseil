package tech.cryptonomic.conseil.api.routes.platform.data.mantis

import tech.cryptonomic.conseil.common.generic.chain.DataTypes.QueryResponse

/** Represents list of endpoints exposed for Mantis Blockchain */
trait MantisDataEndpoints extends MantisDataEndpointsCreator {

  private val platform: String = "mantis"

  def mantisBlocksEndpoint: Endpoint[((String, MantisFilter), Option[String]), Option[List[QueryResponse]]] =
    blocksEndpoint(platform)

  def mantisBlocksHeadEndpoint: Endpoint[(String, Option[String]), Option[QueryResponse]] =
    blocksHeadEndpoint(platform)

  def mantisBlockByHashEndpoint: Endpoint[((String, String), Option[String]), Option[QueryResponse]] =
    blockByHashEndpoint(platform)

  def mantisTransactionsEndpoint: Endpoint[((String, MantisFilter), Option[String]), Option[List[QueryResponse]]] =
    transactionsEndpoint(platform)

  def mantisTransactionByHashEndpoint: Endpoint[((String, String), Option[String]), Option[QueryResponse]] =
    transactionByHashEndpoint(platform)

  def mantisLogsEndpoint: Endpoint[((String, MantisFilter), Option[String]), Option[List[QueryResponse]]] =
    logsEndpoint(platform)

  def mantisReceiptsEndpoint: Endpoint[((String, MantisFilter), Option[String]), Option[List[QueryResponse]]] =
    receiptsEndpoint(platform)

  def mantisContractsEndpoint: Endpoint[((String, MantisFilter), Option[String]), Option[List[QueryResponse]]] =
    contractsEndpoint(platform)

  def mantisTokensEndpoint: Endpoint[((String, MantisFilter), Option[String]), Option[List[QueryResponse]]] =
    tokensEndpoint(platform)

  def mantisTokenTransfersEndpoint: Endpoint[((String, MantisFilter), Option[String]), Option[List[QueryResponse]]] =
    tokenTransfersEndpoint(platform)

  def mantisAccountsEndpoint: Endpoint[((String, MantisFilter), Option[String]), Option[List[QueryResponse]]] =
    accountsEndpoint(platform)

  def mantisAccountByAddressEndpoint: Endpoint[((String, String), Option[String]), Option[QueryResponse]] =
    accountByAddressEndpoint(platform)

}
