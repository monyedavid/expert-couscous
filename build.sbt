name := "expert-couscous"

version := "0.1"

scalaVersion := "2.13.3"

val akkaVersion = "2.5.26"
val akkaHttpVersion = "10.1.11"
val scalaTestVersion = "3.2.0"

libraryDependencies ++= Seq(
  // akka streams
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  // akka http
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  // testing
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test
)
