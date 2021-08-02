import Implicits._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import spray.json._

object Server {

  import DefaultJsonProtocol._
  import system.dispatcher
  import utils._

  val route: Route =
    pathPrefix("org") {
      (path(Segment / "contributors") & get) {
        organization => // /org/{org_name}/contributors
          val responseFuture = GitHub(organization)
          val entityFuture = responseFuture.map { csl =>
            HttpEntity(
              ContentTypes.`application/json`,
              csl.toJson.prettyPrint
            )
          }
          complete(entityFuture)

      }
    }

  def main(args: Array[String]): Unit = {
    Http().bindAndHandle(route, "localhost", 8080)
  }

}
