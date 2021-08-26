package com.david

import cats.effect._
import cats.implicits._
import com.david.utils._
import com.david.utils.JsonFormat._

import com.typesafe.config.{Config, ConfigFactory}
import scala.language.postfixOps

object GitHub {
  val env: Config =
    ConfigFactory
      .load()
      .getConfig(
        "env-development"
      ) // if (System.getenv("SCALA_ENV") == null) "development" else System.getenv("SCALA_ENV")
  val token: String = env.getString("token")
  val nextBatchDifference = 2

  def requestV3[T](uri: String): IO[T] = ???

  def getPaginatedRepos(
      organization: String,
      page: Int = 1,
      per_page: Int = 20
  ): IO[List[Repo]] = ???

  def addToRef[T](
      batchResult: Outcome[IO, Throwable, List[T]],
      ref: Ref[IO, List[T]]
  ): IO[Either[String, List[T]]] =
    batchResult match {
      case Outcome.Succeeded(fa) =>
        for {
          lr <- fa
          _ <- ref.update(lr |+| _)
        } yield Right(lr)
      case Outcome.Errored(e) =>
        IO(Left(e.toString)).debug
      case Outcome.Canceled() => IO(Left("[cancelled]")).debug
    }

  // blocking call
  // recursive parallel acquisition of repositories from organization :)
  def getAllRepos(
      organization: String,
      ref: Ref[IO, List[Repo]],
      signal: Deferred[IO, Unit],
      batch1: Int = 1,
      batch2: Int = 2
  ): IO[Unit] =
    for {
      batch1Fib <- getPaginatedRepos(organization, batch1).start
      batch2Fib <- getPaginatedRepos(organization, batch2).start
      b1R <- batch1Fib.join
      b2R <- batch2Fib.join
      _ <- addToRef(b1R, ref)
      b2Q <- addToRef(b2R, ref)
      _ <- b2Q match {
        case Left(_) => IO.unit
        case Right(value) =>
          if (value.isEmpty) signal.complete()
          else
            getAllRepos(
              organization,
              ref,
              signal,
              batch1 + nextBatchDifference,
              batch2 + nextBatchDifference
            )
      }
    } yield ()

  def getPaginatedContributors(
      organization: String,
      repo: Repo,
      page: Int = 1,
      per_page: Int = 20
  ): IO[List[Contributor]] = ???

  def getAllContributors(
      organization: String,
      repo: Repo,
      ref: Ref[IO, List[Contributor]],
      batch1: Int = 1,
      batch2: Int = 2
  ): IO[Unit] =
    for {
      batch1Fib <- getPaginatedContributors(organization, repo, batch1).start
      batch2Fib <- getPaginatedContributors(organization, repo, batch2).start
      b1R <- batch1Fib.join
      b2R <- batch2Fib.join
      _ <- addToRef(b1R, ref)
      b2Q <- addToRef(b2R, ref)
      _ <- b2Q match {
        case Left(_) => IO.unit
        case Right(value) =>
          if (value.isEmpty) IO.unit
          else
            getAllContributors(
              organization,
              repo,
              ref,
              batch1 + nextBatchDifference,
              batch2 + nextBatchDifference
            )
      }
    } yield ()

  // parallelize computations :)
  def contributionsStatGen(cl: List[Contributor]): Iterable[ContributorsStat] =
    cl.groupMapReduce(identity)(_ => 1)(_ + _).map {
      case (contributor, count) => ContributorsStat(contributor.login, count)
    }

  def apply(organization: String): IO[Iterable[ContributorsStat]] =
    for {
      reposRef <- Ref[IO, List[Repo]].of(List[Repo]())
      contributorsRef <- Ref[IO, List[Repo]].of(List[Contributor]())
      reposSignal <- Deferred[IO, Unit]
      _ <- getAllRepos(organization, reposRef, reposSignal)
      _ <-
        reposSignal.get // blocking call -> wait for allRepos to be retrieved :)
      repos <- reposRef.get
      _ <-
        repos
          .map(repo => getAllContributors(organization, repo, contributorsRef))
          .parSequence
          .void
      contributors <- contributorsRef.get
    } yield contributionsStatGen(contributors)

}
