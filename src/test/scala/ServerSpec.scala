import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{MethodRejection, Route}
import scala.concurrent.Await
import scala.concurrent.duration._


import spray.json._
import utils._

class ServerSpec extends AnyWordSpec with Matchers with ScalatestRouteTest {

}
