import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import spray.json.JsValue
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import spray.json._


/**
 * GITHUB API:
 *
 * TODO:
 *   - use custom graphs(akka.streams) to retrieve all repos & contributors/repo (from paginated api) \\ MAX per_page=100
 *   - token in resources/config
 */
object GitHub {

	implicit val system: ActorSystem = ActorSystem()
	implicit val materialize: ActorMaterializer = ActorMaterializer()

	import system.dispatcher

	import DefaultJsonProtocol._

	case class Repos(name: String)

	case class Contributor(login: String)

	case class ContributorsStat(login: String, contributions: Int)

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

	def httpRequest(uri: String): Future[HttpResponse] =
		Http().singleRequest(HttpRequest(
			method = HttpMethods.GET,
			uri = uri
		)
			.withHeaders(
				RawHeader("Authorization", "token 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"), RawHeader("Accept", "application/vnd.github.v3+json")
			))

	def getRepos(organization: String, pgn: Int = 1): Future[List[Repos]] =
		httpRequest(s"https://api.github.com/orgs/$organization/repos?per_page=100&pgn=$pgn").flatMap(_.entity.toStrict(3 minutes))
			.map(_.data.utf8String).map(_.parseJson.convertTo[List[Repos]])

	def getContributors(organization: String, repo: Repos, pgn: Int = 1): Future[List[Contributor]] =
		httpRequest(s"https://api.github.com/repos/$organization/${repo.name}/collaborators?per_page=100&pgn=$pgn").flatMap(_.entity.toStrict(3 minutes))
			.map(_.data.utf8String).map(_.parseJson.convertTo[List[Contributor]])

	def apply(organization: String): Future[List[ContributorsStat]] = {

		//  Future[List[String]] -> List[String] -> List[Future[List[String]]] -> Future[List[String]]

		/**
		 * Step 1: GET org repos ✅  <br/>
		 * Step 2: GET contributors/repos ✅ <br/>
		 * Step 2.5: CONVERT `Future[List[Repo]]  repoList => List[contributors]` ✅ <br/>
		 * Step 3: COUNT contributors  | { “name”: <contributor_login>, “contributions”: <no_of_contributions> } \\ group by & count
		 */

		// List[Repo](repo -> List[Contributors])  => Future[List[List[Contributor]]] => Future[List[Contributor]]
		val repoAndContributorsFuture = getRepos(organization).flatMap { repoList => Future sequence repoList.flatMap(repo => List(getContributors(organization, repo))) }.map(lol => lol.flatten)


		???
	}


}
