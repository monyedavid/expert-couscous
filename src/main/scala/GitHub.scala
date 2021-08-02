import Implicits._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import com.typesafe.config.{Config, ConfigFactory}
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * GITHUB API:
  *
  * TODO:
  *   - use custom graphs(akka.streams) to retrieve all repos & contributors/repo (from paginated api) \\ MAX per_page=20
  */
object GitHub {

  import DefaultJsonProtocol._
  import system.dispatcher
  import utils._

  val env: Config =
    ConfigFactory
      .load()
      .getConfig(
        "env-development"
      ) // if (System.getenv("SCALA_ENV") == null) "development" else System.getenv("SCALA_ENV")
  val token: String = env.getString("token")

  def requestV3(uri: String): Future[HttpResponse] =
    Http().singleRequest(
      HttpRequest(
        method = HttpMethods.GET,
        uri = uri
      ).withHeaders(
        RawHeader("Authorization", s"token $token"),
        RawHeader("Accept", "application/vnd.github.v3+json")
      )
    )

  def getRepos(
      organization: String,
      per_page: Int = 20,
      page: Int = 1
  ): Future[List[Repo]] =
    requestV3(
      s"https://api.github.com/orgs/$organization/repos?per_page=$per_page&page=$page"
    ).flatMap(_.entity.toStrict(3 minutes))
      .map(_.data.utf8String)
      .map(_.parseJson.convertTo[List[Repo]])

  def getContributors(
      organization: String,
      repo: Repo,
      per_page: Int = 20,
      page: Int = 1
  ): Future[List[Contributor]] =
    requestV3(
      s"https://api.github.com/repos/$organization/${repo.name}/collaborators?per_page=$per_page&page=$page"
    ).flatMap(_.entity.toStrict(3 minutes))
      .map(_.data.utf8String)
      .map(_.parseJson.convertTo[List[Contributor]])

  def contributionsStatGen(cl: List[Contributor]): Iterable[ContributorsStat] =
    cl.groupMapReduce(x => x)(_ => 1)(_ + _).map {
      case (contributor, count) => ContributorsStat(contributor.login, count)
    }

  def apply(organization: String): Future[Iterable[ContributorsStat]] = {

    /**
      * Step 1: GET org repos ✅  <br/>
      * Step 2: GET contributors/repos ✅ <br/>
      * Step 2.5: CONVERT `Future[List[Repo]]  repoList => List[contributors]` ✅ <br/>
      * Step 3: COUNT contributors  | { “name”: <contributor_login>, “contributions”: <no_of_contributions> } \\ group by & count ✅
      */

    // List[Repo](repo -> List[Contributors])  => Future[List[List[Contributor]]] => Future[List[Contributor]]
    val repoAndContributorsFuture: Future[List[Contributor]] =
      for {
        repoList <- getRepos(organization)
        contributors = repoList.map(repo => getContributors(organization, repo))
        lol <- Future.sequence(contributors)
      } yield lol.flatten

    repoAndContributorsFuture.map(contributionsStatGen)
  }

}

//      getRepos(organization)
//        .flatMap { repoList =>
//          Future sequence repoList
//            .map(repo => getContributors(organization, repo))
//        }
//        .map(lol => lol.flatten)
