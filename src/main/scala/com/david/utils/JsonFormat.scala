package com.david.utils

// import spray.json._

object JsonFormat {

  case class Repo(name: String)

  case class Contributor(login: String)

  case class ContributorsStat(name: String, contributions: Int)

  //  implicit val repoJsonFormat: RootJsonFormat[Repo] = jsonFormat1(Repo)
  //

  //  implicit val contributorJsonFormat: RootJsonFormat[Contributor] = jsonFormat1(
  //    Contributor
  //  )
  //
  //  // convert contributor stat to json
  //  implicit val contributorStatJsonFormat: RootJsonFormat[ContributorsStat] =
  //    jsonFormat2(ContributorsStat)
}
