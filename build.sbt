import Dependencies._
import sbt.Keys.{scalaVersion, _}
import sbt.Package.ManifestAttributes
import sbt.{Resolver, _}


// allow `+` prefix for cross-building project: https://github.com/sbt/sbt/issues/3422
lazy val build = taskKey[Unit]("Build, test, and package artifacts for publishing")

lazy val Scala212 = "2.12.6"
lazy val Scala211 = "2.11.12"

val commonSettings = Seq(
  homepage := Some(new URL("https://github.com/evolution-gaming/play-json-tools")),
  resolvers += Resolver.bintrayRepo("evolutiongaming", "maven"),
  organizationName := "Evolution Gaming",
  organizationHomepage := Some(url("http://evolutiongaming.com")),
  bintrayOrganization := Some("evolutiongaming"),
  releaseCrossBuild := true,
  organization := "com.evolutiongaming",
  licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
  description := "Set of implicit helper classes for transforming various objects to and from JSON",
  startYear := Some(2017),
  javaOptions in(Compile, doc) := Seq(),
  publishArtifact in(Compile, packageDoc) := false,
  publishArtifact in packageDoc := false,
  sources in(Compile, doc) := Seq(),

  packageOptions := {
    Seq(ManifestAttributes(
      ("Implementation-Version", (version in ThisProject).value)
    ))
  },

  scalaVersion := Scala212,
  crossScalaVersions := Seq(Scala212, Scala211),

  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-Xfatal-warnings",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Xfuture"
  )
)

lazy val playJsonGeneric = (project in file("play-json-generic"))
  .settings(build := {((test in Test).value, packagedArtifacts.value)})
  .settings(commonSettings)
  .settings(Seq(
    moduleName := "play-json-generic",
    name := "play-json-generic"
  ))
  .settings(libraryDependencies ++= Seq(shapeless, playJson, scalaTest).map(excludeLog4j))

lazy val playJsonTools = (project in file("play-json-tools"))
  .settings(build := {((test in Test).value, packagedArtifacts.value)})
  .settings(commonSettings)
  .settings(Seq(
    moduleName := "play-json-tools",
    name := "play-json-tools"
  ))
  .settings(libraryDependencies ++= Seq(playJson, nel, scalaTest).map(excludeLog4j))