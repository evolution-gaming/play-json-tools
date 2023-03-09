import Dependencies._

val commonSettings = Seq(
  homepage := Some(new URL("https://github.com/evolution-gaming/play-json-tools")),
  publishTo := Some(Resolver.evolutionReleases),
  organizationName := "Evolution",
  organizationHomepage := Some(url("https://evolution.com")),
  releaseCrossBuild := true,
  organization := "com.evolution",
  licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT"))),
  description := "Set of implicit helper classes for transforming various objects to and from JSON",
  startYear := Some(2017),
  crossScalaVersions := Seq("2.13.10", "2.12.17"),
  scalaVersion := crossScalaVersions.value.head,
)

lazy val root = project
  .in(file("."))
  .disablePlugins(MimaPlugin)
  .settings(commonSettings)
  .settings(
    publish / skip := true
  )
  .aggregate(
    `play-json-tools`,
    `play-json-genericJVM`,
    `play-json-genericJS`,
    `play-json-jsoniterJVM`,
    `play-json-jsoniterJS`,
    `play-json-circe`
  )

lazy val `play-json-genericJVM` = `play-json-generic`.jvm

lazy val `play-json-genericJS` = `play-json-generic`.js

lazy val `play-json-generic` = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(commonSettings)
  .settings(
    scalacOptsFailOnWarn := Some(false),
    libraryDependencies ++= Seq(
      shapeless,
      playJson,
      scalaTest % Test
    ).map(excludeLog4j)
  )

lazy val `play-json-tools` = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      playJson,
      nel,
      scalaTest % Test
    ).map(excludeLog4j)
  )

lazy val `play-json-jsoniterJVM` = `play-json-jsoniter`.jvm

lazy val `play-json-jsoniterJS` = `play-json-jsoniter`.js

lazy val `play-json-jsoniter` = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Full)
  .settings(commonSettings)
  .settings(
    crossScalaVersions := crossScalaVersions.value ++ Seq("3.2.2"),
    libraryDependencies ++= (Seq(
      playJson,
      jsoniter,
      collectionCompact,
      scalaTest % Test
    ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 13 =>
        Seq(jsonGenerator % Test)
      case _ =>
        Seq()
    })).map(excludeLog4j)
  )

lazy val `play-json-circe` = project
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      playJson,
      circe.core,
      circe.parser,
      scalaTest % Test
    ).map(excludeLog4j)
  )