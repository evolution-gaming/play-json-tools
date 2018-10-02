import Dependencies._


val commonSettings = Seq(
  homepage := Some(new URL("https://github.com/evolution-gaming/play-json-tools")),
  resolvers += Resolver.bintrayRepo("evolutiongaming", "maven"),
  organizationName := "Evolution Gaming",
  organizationHomepage := Some(url("http://evolutiongaming.com")),
  bintrayOrganization := Some("evolutiongaming"),
  releaseCrossBuild := true,
  organization := "com.evolutiongaming",
  licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT"))),
  description := "Set of implicit helper classes for transforming various objects to and from JSON",
  startYear := Some(2017),
  scalaVersion := crossScalaVersions.value.last,
  crossScalaVersions := Seq("2.11.12", "2.12.6"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-Xfatal-warnings",
    //"-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Xfuture"
  )
)

lazy val root = (project in file(".")
  settings (name := "play-json-tools")
  settings commonSettings
  settings (skip in publish := true)
  aggregate(
    playJsonTools,
    playJsonGeneric))

lazy val playJsonGeneric = (project in file("play-json-generic"))
  .settings(commonSettings)
  .settings(Seq(
    moduleName := "play-json-generic",
    name := "play-json-generic"))
  .settings(libraryDependencies ++= Seq(shapeless, playJson, scalaTest).map(excludeLog4j))

lazy val playJsonTools = (project in file("play-json-tools"))
  .settings(commonSettings)
  .settings(Seq(
    moduleName := "play-json-tools",
    name := "play-json-tools"))
  .settings(libraryDependencies ++= Seq(playJson, nel, scalaTest).map(excludeLog4j))