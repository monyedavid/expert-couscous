import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Implicits {

  implicit val system: ActorSystem = ActorSystem("HighLevelExample")
  implicit val materialize: ActorMaterializer = ActorMaterializer()
}
