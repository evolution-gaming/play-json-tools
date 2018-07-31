import sbt._


object Dependencies {
  private val playVersion = "2.6.9"

  val shapeless = "com.chuusai" %% "shapeless" % "2.3.3"

  val nel = "com.evolutiongaming" %% "nel" % "1.2"

  val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.5" % Test

  val playJson: ModuleID = "com.typesafe.play" %% "play-json" % playVersion % Provided

  def excludeLog4j(moduleID: ModuleID): ModuleID = moduleID.excludeAll(
    ExclusionRule("log4j", "log4j"),
    ExclusionRule("org.slf4j", "slf4j-log4j12"),
    ExclusionRule("commons-logging", "commons-logging"))
}