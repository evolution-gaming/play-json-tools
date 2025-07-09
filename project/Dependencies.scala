import sbt._

object Dependencies {

  val shapeless = "com.chuusai"         %% "shapeless" % "2.3.10"
  val nel       = "com.evolutiongaming" %% "nel"       % "1.3.5"
  val playJson  = "com.typesafe.play"   %% "play-json" % "2.10.7"
  val scalaTest = "org.scalatest"       %% "scalatest" % "3.2.19"
  val jsoniter  = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"  % "2.36.7"
  val jsonGenerator = "com.github.imrafaelmerino" %% "json-scala-values-generator" % "1.0.0"
  val collectionCompact = "org.scala-lang.modules" %% "scala-collection-compat" % "2.10.0"

  object circe {
    val version  = "0.14.14"
    val core     = "io.circe" %% "circe-core"   % version
    val parser   = "io.circe" %% "circe-parser" % version
  }

  def excludeLog4j(moduleID: ModuleID): ModuleID = moduleID.excludeAll(
    ExclusionRule("log4j", "log4j"),
    ExclusionRule("org.slf4j", "slf4j-log4j12"),
    ExclusionRule("commons-logging", "commons-logging"))
}
