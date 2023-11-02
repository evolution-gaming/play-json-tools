import Dependencies._
import ReleaseTransformations._

val Scala213 = "2.13.12"
val Scala212 = "2.12.18"
val Scala3   = "3.3.1"

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
  scalaVersion := Scala213,
  crossScalaVersions := Seq(scalaVersion.value, Scala212),
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

lazy val root = project
  .in(file("."))
  .disablePlugins(MimaPlugin)
  .settings(
    commonSettings,
    publish / skip := true,
    crossScalaVersions := Nil,

    /* Support uneven cross scala versions in sub-projects.
     * See https://www.scala-sbt.org/1.x/docs/Cross-Build.html#Note+about+sbt-release
     */
    releaseCrossBuild := false,
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      releaseStepCommandAndRemaining("+test"),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      releaseStepCommandAndRemaining("+publish"),
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
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
    scalacOptsFailOnWarn := Some(false),
    libraryDependencies ++= Seq(
      shapeless,
      playJson,
      scalaTest % Test
    ).map(excludeLog4j)
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
    crossScalaVersions := crossScalaVersions.value ++ Seq(Scala3),
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
