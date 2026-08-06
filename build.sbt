import Dependencies.*

import scala.collection.Seq

val Scala213 = "2.13.16"
val Scala3   = "3.3.8"

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
  crossScalaVersions := Seq(scalaVersion.value, Scala3),
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        List(
          "-Xsource:3",
        )
      case _ =>
        List(
          // improve error messages:
          "-explain",
          "-explain-types",
        )
    }
  },
)

// releases of this build are expected to keep binary compatibility; set this to
// `Compatibility.None` in the commit that intends to break it, which is what lets `check` tell a
// deliberate break from an accidental one
ThisBuild / versionPolicyIntention := Compatibility.BinaryCompatible

val alias: Seq[sbt.Def.Setting[?]] =
  // the Scala version is left to the caller, since CI runs `check` once per version in its matrix
  addCommandAlias("check", "all versionPolicyCheck Compile/doc") ++
    addCommandAlias("build", "+all compile test")

lazy val root = project
  .in(file("."))
  .disablePlugins(MimaPlugin)
  .settings(alias)
  .settings(
    commonSettings,
    publish / skip := true,
    name := "play-json-tools",
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
  .settings(
    commonSettings,
    allowUnsafeScalaLibUpgrade := true,
    libraryDependencies ++= (Seq(
      playJson,
      scalaTest % Test
    ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
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
    allowUnsafeScalaLibUpgrade := true,
    libraryDependencies ++= (Seq(
      playJson,
      jsoniter,
      collectionCompact,
      scalaTest % Test
    ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Seq(jsonGenerator % Test)
      case _ =>
        Seq()
    })).map(excludeLog4j)
  )

// not part of the aggregate, benchmarks are run manually
lazy val benchmark = project
  .dependsOn(
    `play-json-jsoniterJVM` % "test->test;compile->compile",
    `play-json-circe` % "test->test;compile->compile")
  .disablePlugins(MimaPlugin)
  .enablePlugins(JmhPlugin)
  .settings(
    commonSettings,
    publish / skip := true,
    crossScalaVersions := Seq(Scala213),
    Jmh / sourceDirectory := (Test / sourceDirectory).value,
    Jmh / classDirectory := (Test / classDirectory).value,
    Jmh / dependencyClasspath := (Test / dependencyClasspath).value,
  )

lazy val `play-json-circe` = project
  .settings(
    commonSettings,
    allowUnsafeScalaLibUpgrade := true,
    libraryDependencies ++= Seq(
      playJson,
      circe.core,
      circe.parser,
      scalaTest % Test
    ).map(excludeLog4j)
  )
