externalResolvers += Resolver.bintrayIvyRepo("evolutiongaming", "sbt-plugins")

val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.11.0")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.2.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.4")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.2")

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.1.0")

addSbtPlugin("com.evolution" % "sbt-scalac-opts-plugin" % "0.0.9")

addSbtPlugin("com.evolution" % "sbt-artifactory-plugin" % "0.0.2")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3")