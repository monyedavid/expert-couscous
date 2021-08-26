val scala3Version = "2.13.6"

val Http4sVersion = "0.23.1"
val CirceVersion = "0.14.1"
val scalaTestVersion = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "expert-couscous",
    version := "0.1.0",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.2.2",
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "com.typesafe" % "config" % "1.4.1",
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    )
  )
