import spray.json._

object utils {

	import DefaultJsonProtocol._

	case class Repos(name: String)

	case class Contributor(login: String)

	case class ContributorsStat(name: String, contributions: Int)

	// extract import information (repo-name) from api
	implicit val repoJsonFormat: RootJsonFormat[Repos] = new RootJsonFormat[Repos] {
		def write(repos: Repos): JsValue = ???

		def read(json: JsValue): Repos = json.asJsObject.getFields("name") match {
			case Seq(JsString(name)) => Repos(name)
			case _ => throw DeserializationException("Repos expected")
		}
	}

	// extract import information (contributor - login/username) from api
	implicit val contributorJsonFormat: RootJsonFormat[Contributor] = new RootJsonFormat[Contributor] {

		override def write(obj: Contributor): JsValue = ???

		override def read(json: JsValue): Contributor = json.asJsObject.getFields("login") match {
			case Seq(JsString(login)) => Contributor(login)
			case _ => throw DeserializationException("Repos expected")
		}
	}

	// convert contributor stat to json
	implicit val contributorStatJsonFormat: RootJsonFormat[ContributorsStat] = jsonFormat2(ContributorsStat)
}
