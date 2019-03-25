package tech.cryptonomic.conseil

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import tech.cryptonomic.conseil.config.ConseilAppConfig
import tech.cryptonomic.conseil.directives.EnableCORSDirectives
import tech.cryptonomic.conseil.routes._
import tech.cryptonomic.conseil.routes.openapi.OpenApiDoc
import tech.cryptonomic.conseil.tezos.{ApiOperations, TezosPlatformDiscoveryOperations}

import scala.concurrent.ExecutionContextExecutor

object Conseil extends App with LazyLogging with EnableCORSDirectives with ConseilAppConfig with FailFastCirceSupport {

  applicationConfiguration match {
    case Right((server, platforms, securityApi, caching)) =>

      val validateApiKey = headerValueByName("apikey").tflatMap[Tuple1[String]] {
        case Tuple1(apiKey) if securityApi.validateApiKey(apiKey) =>
          provide(apiKey)
        case _ =>
          complete((Unauthorized, "Incorrect API key"))
      }

      implicit val system: ActorSystem = ActorSystem("conseil-system")
      implicit val materializer: ActorMaterializer = ActorMaterializer()
      implicit val executionContext: ExecutionContextExecutor = system.dispatcher

      val tezosDispatcher = system.dispatchers.lookup("akka.tezos-dispatcher")
      lazy val tezos = Tezos(tezosDispatcher)
      lazy val tezosPlatformDiscoveryOperations = TezosPlatformDiscoveryOperations(ApiOperations)(executionContext)
      tezosPlatformDiscoveryOperations.init()
      lazy val platformDiscovery = PlatformDiscovery(platforms, caching, tezosPlatformDiscoveryOperations)(tezosDispatcher)
      lazy val data = Data(platforms)(tezosDispatcher)

      val route = cors() {
        enableCORS {
          validateApiKey { _ =>
            logRequest("Conseil", Logging.DebugLevel) {
              tezos.route ~
              AppInfo.route
            } ~
            logRequest("Metadata Route", Logging.DebugLevel) {
              platformDiscovery.route
            } ~
            logRequest("Data Route", Logging.DebugLevel) {
              data.getRoute ~ data.postRoute
            }
          } ~
          options {
            // Support for CORS pre-flight checks.
            complete("Supported methods : GET and POST.")
          }
        }
      } ~
      pathPrefix("docs") {
        pathEndOrSingleSlash {
          getFromResource("web/index.html")
        }
      } ~
      pathPrefix("swagger-ui") {
        getFromResourceDirectory("web/swagger-ui/")
      } ~
      path("openapi.json") {
        complete(OpenApiDoc.openapiJson)
      }

      val bindingFuture = Http().bindAndHandle(route, server.hostname, server.port)
      logger.info(
        """
          | =========================***=========================
          |  Conseil v.{}
          |  {}
          | =========================***=========================
          |
          |  Bonjour...
          |""".stripMargin,
        BuildInfo.version,
        BuildInfo.gitHeadCommit.fold("")(hash => s"[commit-hash: ${hash.take(7)}]")
      )

      sys.addShutdownHook {
        bindingFuture
          .flatMap(_.unbind().andThen { case _ => logger.info("Server stopped...") })
          .flatMap(_ => system.terminate())
          .onComplete(_ => logger.info("We're done here, nothing else to see"))
      }

    case Left(errors)
    =>
    //nothing to do
  }

}
