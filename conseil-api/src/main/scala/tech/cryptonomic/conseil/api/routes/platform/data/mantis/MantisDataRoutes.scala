package tech.cryptonomic.conseil.api.routes.platform.data.mantis

import tech.cryptonomic.conseil.api.metadata.MetadataService
import tech.cryptonomic.conseil.common.config.MetadataConfiguration
import tech.cryptonomic.conseil.common.metadata.PlatformPath

import scala.concurrent.ExecutionContext

/** Represents the data routes for Mantis Blockchain */
case class MantisDataRoutes(
    metadataService: MetadataService,
    metadataConfiguration: MetadataConfiguration,
    operations: MantisDataOperations,
    maxQueryResultSize: Int
)(implicit val executionContext: ExecutionContext)
    extends MantisDataRoutesCreator {
  override val platform: PlatformPath = PlatformPath("mantis")
}
