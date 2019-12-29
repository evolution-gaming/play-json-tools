import sbt._


object Dependencies {

  val shapeless = "com.chuusai"         %% "shapeless" % "2.3.3"
  val nel       = "com.evolutiongaming" %% "nel"       % "1.3.4"
  val playJson  = "com.typesafe.play"   %% "play-json" % "2.7.4"
  val scalaTest = "org.scalatest"       %% "scalatest" % "3.1.0"
  val jsoniter  = "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"  % "2.0.4"
  val generator = "com.github.imrafaelmerino" %% "json-scala-values-generator" % "1.0.0"

  def excludeLog4j(moduleID: ModuleID): ModuleID = moduleID.excludeAll(
    ExclusionRule("log4j", "log4j"),
    ExclusionRule("org.slf4j", "slf4j-log4j12"),
    ExclusionRule("commons-logging", "commons-logging"))
}