import Dependencies.*

import scala.collection.Seq

val Scala213 = "2.13.16"
val Scala212 = "2.12.20"
val Scala3   = "3.3.6"

val commonSettings = Seq(
  homepage := Some(url("https://github.com/evolution-gaming/play-json-tools")),
  publishTo := Some(Resolver.evolutionReleases),
  organizationName := "Evolution",
  organizationHomepage := Some(url("https://evolution.com")),
  organization := "com.evolution",
  licenses := Seq(("MIT", url("https://opensource.org/licenses/MIT"))),
  description := "Set of implicit helper classes for transforming various objects to and from JSON",
  startYear := Some(2017),
  scalaVersion := Scala213,
  crossScalaVersions := Seq(scalaVersion.value, Scala212, Scala3),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 13 =>
        List(
          "-Xsource:3",
        )
      case _ =>
        Nil
    }
  },
)

val alias: Seq[sbt.Def.Setting[?]] =
  //  addCommandAlias("check", "all versionPolicyCheck Compile/doc") ++
  addCommandAlias("check", "show version") ++
    addCommandAlias("build", "+all compile test")

lazy val root = project
  .in(file("."))
  .disablePlugins(MimaPlugin)
  .settings(alias)
  .settings(
    commonSettings,
    publish / skip := true,
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
  .settings(crossScalaVersions -= Scala3)

lazy val `play-json-genericJS` = `play-json-generic`.js
  .settings(crossScalaVersions -= Scala3)

lazy val `play-json-generic` = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .settings(
    commonSettings,
    libraryDependencies ++= (Seq(
      playJson,
      scalaTest % Test
    ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, v)) if v >= 12 =>
        Seq(shapeless)
      case _ =>
        Seq()
    })).map(excludeLog4j)
  )

lazy val `play-json-tools` = project
  .settings(
    commonSettings,
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
  .settings(
    commonSettings,
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
