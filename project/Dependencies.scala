import sbt._

object Dependencies {

  val shapeless = "com.chuusai"         %% "shapeless" % "2.3.3"
  val nel       = "com.evolutiongaming" %% "nel"       % "1.3.4"
  val playJson  = "com.typesafe.play"   %% "play-json" % "2.9.2"
  val scalaTest = "org.scalatest"       %% "scalatest" % "3.1.2"
  val jsoniter  = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"  % "2.7.3"
  val jsonGenerator = "com.github.imrafaelmerino" %% "json-scala-values-generator" % "1.0.0"
  val collectionCompact = "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.3"

  def excludeLog4j(moduleID: ModuleID): ModuleID = moduleID.excludeAll(
    ExclusionRule("log4j", "log4j"),
    ExclusionRule("org.slf4j", "slf4j-log4j12"),
    ExclusionRule("commons-logging", "commons-logging"))
}