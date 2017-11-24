import sbt._


object Dependencies {
  private val fasterXmlVersion = "2.9.2"
  private val playVersion = "2.6.7"

  val jacksonDatabind: ModuleID = "com.fasterxml.jackson.core" % "jackson-databind" % fasterXmlVersion % Compile
  
  val scalaTools: ModuleID = "com.evolutiongaming" %% "scala-tools" % "1.15"

  val nel = "com.evolutiongaming" %% "nel" % "1.0"

  val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % "3.0.4" % Test

  val playJson: ModuleID = "com.typesafe.play" %% "play-json" % playVersion % Compile excludeAll (
    ExclusionRule("org.scala-lang", "scala-reflect"),
    ExclusionRule("com.fasterxml.jackson.core", "jackson-annotations"),
    ExclusionRule("com.fasterxml.jackson.core", "jackson-core"),
    ExclusionRule("com.fasterxml.jackson.core", "jackson-databind"))

  def excludeLog4j(moduleID: ModuleID): ModuleID = moduleID.excludeAll(
    ExclusionRule("log4j", "log4j"),
    ExclusionRule("org.slf4j", "slf4j-log4j12"),
    ExclusionRule("commons-logging", "commons-logging"))
}