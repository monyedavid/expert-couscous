import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import spray.json._

object Server {

  implicit val system: ActorSystem = ActorSystem("HighLevelExample")
  implicit val materialize: ActorMaterializer = ActorMaterializer()

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
