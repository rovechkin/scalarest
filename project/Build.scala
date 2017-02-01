import sbt._
import Keys._
import sbtassembly.AssemblyKeys._

object Build extends Build {
  lazy val root = (project in file(".")).
    aggregate(client)

  lazy val client = Project(id = "client", base = file("client")).settings(
    name := "client",
    version := "1.0",
    scalaVersion := "2.11.6",
    libraryDependencies ++= Seq(
      "com.github.scopt" %% "scopt" % "3.3.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
    ),
    test in assembly :={},
    resolvers += Resolver.sonatypeRepo("public"),
    fork in run := true
  )

  lazy val assemblySettings = Seq(
    test in assembly := {} //noisy for end-user since the jar is not available and user needs to build the project locally
  )
}