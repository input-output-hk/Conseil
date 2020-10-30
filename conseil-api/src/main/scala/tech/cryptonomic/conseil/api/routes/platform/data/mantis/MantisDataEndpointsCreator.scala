package tech.cryptonomic.conseil.api.routes.platform.data.mantis

import tech.cryptonomic.conseil.api.routes.platform.data.{ApiDataEndpoints, ApiDataJsonSchemas}
import tech.cryptonomic.conseil.common.generic.chain.DataTypes.QueryResponse

/** Trait containing endpoints definitions for Mantis-related blockchains */
trait MantisDataEndpointsCreator extends ApiDataEndpoints with ApiDataJsonSchemas with MantisFilterFromQueryString {

  /** V2 Blocks endpoint definition */
  private[mantis] def blocksEndpoint(platform: String): Endpoint[((String, MantisFilter), Option[String]), Option[
    List[QueryResponse]
  ]] =
    endpoint(
      request = get(url = createPath(platform) / "blocks" /? mantisQsFilter, headers = optHeader("apiKey")),
      response = compatibilityQuery[List[QueryResponse]]("blocks"),
      tags = createTags(platform, "Blocks")
    )

  /** V2 Blocks head endpoint definition */
  private[mantis] def blocksHeadEndpoint(
      platform: String
  ): Endpoint[(String, Option[String]), Option[QueryResponse]] =
    endpoint(
      request = get(url = createPath(platform) / "blocks" / "head", headers = optHeader("apiKey")),
      response = compatibilityQuery[QueryResponse]("blocks head"),
      tags = createTags(platform, "Blocks")
    )

  /** V2 Blocks by hash endpoint definition */
  private[mantis] def blockByHashEndpoint(
      platform: String
  ): Endpoint[((String, String), Option[String]), Option[QueryResponse]] =
    endpoint(
      request =
        get(url = createPath(platform) / "blocks" / segment[String](name = "hash"), headers = optHeader("apiKey")),
      response = compatibilityQuery[QueryResponse]("block by hash"),
      tags = createTags(platform, "Blocks")
    )

  /** V2 Transactions endpoint definition */
  private[mantis] def transactionsEndpoint(
      platform: String
  ): Endpoint[((String, MantisFilter), Option[String]), Option[
    List[QueryResponse]
  ]] =
    endpoint(
      request = get(url = createPath(platform) / "transactions" /? mantisQsFilter, headers = optHeader("apiKey")),
      response = compatibilityQuery[List[QueryResponse]]("transactions"),
      tags = createTags(platform, "Transactions")
    )

  /** V2 Transaction by id endpoint definition */
  private[mantis] def transactionByHashEndpoint(
      platform: String
  ): Endpoint[((String, String), Option[String]), Option[QueryResponse]] =
    endpoint(
      request = get(
        url = createPath(platform) / "transactions" / segment[String](name = "hash"),
        headers = optHeader("apiKey")
      ),
      response = compatibilityQuery[QueryResponse]("transaction by hash"),
      tags = createTags(platform, "Transactions")
    )

  /** V2 Logs endpoint definition */
  private[mantis] def logsEndpoint(platform: String): Endpoint[((String, MantisFilter), Option[String]), Option[
    List[QueryResponse]
  ]] =
    endpoint(
      request = get(url = createPath(platform) / "logs" /? mantisQsFilter, headers = optHeader("apiKey")),
      response = compatibilityQuery[List[QueryResponse]]("logs"),
      tags = createTags(platform, "Logs")
    )

  /** V2 Receipts endpoint definition */
  private[mantis] def receiptsEndpoint(platform: String): Endpoint[((String, MantisFilter), Option[String]), Option[
    List[QueryResponse]
  ]] =
    endpoint(
      request = get(url = createPath(platform) / "receipts" /? mantisQsFilter, headers = optHeader("apiKey")),
      response = compatibilityQuery[List[QueryResponse]]("receipts"),
      tags = createTags(platform, "Receipts")
    )

  /** V2 Contracts endpoint definition */
  private[mantis] def contractsEndpoint(
      platform: String
  ): Endpoint[((String, MantisFilter), Option[String]), Option[
    List[QueryResponse]
  ]] =
    endpoint(
      request = get(url = createPath(platform) / "contracts" /? mantisQsFilter, headers = optHeader("apiKey")),
      response = compatibilityQuery[List[QueryResponse]]("contracts"),
      tags = createTags(platform, "Contracts")
    )

  /** V2 Tokens endpoint definition */
  private[mantis] def tokensEndpoint(platform: String): Endpoint[((String, MantisFilter), Option[String]), Option[
    List[QueryResponse]
  ]] =
    endpoint(
      request = get(url = createPath(platform) / "tokens" /? mantisQsFilter, headers = optHeader("apiKey")),
      response = compatibilityQuery[List[QueryResponse]]("tokens"),
      tags = createTags(platform, "Tokens")
    )

  /** V2 Token transfers endpoint definition */
  private[mantis] def tokenTransfersEndpoint(
      platform: String
  ): Endpoint[((String, MantisFilter), Option[String]), Option[
    List[QueryResponse]
  ]] =
    endpoint(
      request = get(url = createPath(platform) / "token_transfers" /? mantisQsFilter, headers = optHeader("apiKey")),
      response = compatibilityQuery[List[QueryResponse]]("token transfers"),
      tags = createTags(platform, "Token transfers")
    )

  /** V2 Accounts endpoint definition */
  private[mantis] def accountsEndpoint(platform: String): Endpoint[((String, MantisFilter), Option[String]), Option[
    List[QueryResponse]
  ]] =
    endpoint(
      request = get(url = createPath(platform) / "accounts" /? mantisQsFilter, headers = optHeader("apiKey")),
      response = compatibilityQuery[List[QueryResponse]]("accounts"),
      tags = createTags(platform, "Accounts")
    )

  /** V2 Accounts by address endpoint definition */
  private[mantis] def accountByAddressEndpoint(
      platform: String
  ): Endpoint[((String, String), Option[String]), Option[QueryResponse]] =
    endpoint(
      request =
        get(url = createPath(platform) / "accounts" / segment[String](name = "address"), headers = optHeader("apiKey")),
      response = compatibilityQuery[QueryResponse]("account by address"),
      tags = createTags(platform, "Accounts")
    )

  private def createPath(platform: String): Path[String] =
    path / "v2" / "data" / platform / segment[String](name = "network")

  private def createTags(platform: String, tag: String): List[String] = List(s"$platform $tag")

}
