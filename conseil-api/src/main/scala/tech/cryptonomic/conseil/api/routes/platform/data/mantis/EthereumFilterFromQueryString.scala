package tech.cryptonomic.conseil.api.routes.platform.data.mantis

import cats.syntax.functor._
import endpoints.algebra.JsonEntities
import tech.cryptonomic.conseil.api.routes.platform.data.ApiFilter.Sorting
import tech.cryptonomic.conseil.api.routes.platform.data.ApiFilterQueryString
import tech.cryptonomic.conseil.common.util.TupleFlattenUtil.FlattenHigh._
import tech.cryptonomic.conseil.common.util.TupleFlattenUtil._

private[mantis] trait MantisFilterFromQueryString extends ApiFilterQueryString { self: JsonEntities =>

  /** Query params type alias */
  type MantisQueryParams = (
      Option[Int],
      Set[String],
      Set[String],
      Set[String],
      Set[String],
      Option[String],
      Option[Sorting]
  )

  /** Function for extracting query string with query params */
  private def filterQs: QueryString[MantisQueryParams] = {
    val raw = limit &
          qs[Set[String]]("block_id") &
          qs[Set[String]]("block_hash") &
          qs[Set[String]]("transaction_id") &
          qs[Set[String]]("account_addresses") &
          sortBy &
          order

    raw map (flatten(_))
  }

  /** Function for mapping query string to Filter */
  val mantisQsFilter: QueryString[MantisFilter] = filterQs.map(MantisFilter.tupled)

}
