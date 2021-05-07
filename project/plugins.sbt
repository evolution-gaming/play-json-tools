externalResolvers += Resolver.bintrayIvyRepo("evolutiongaming", "sbt-plugins")

val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.5.1")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.6")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.7.0")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.15")

addSbtPlugin("com.evolutiongaming" % "sbt-scalac-opts-plugin" % "0.0.5")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.8.1")
