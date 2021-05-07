externalResolvers += Resolver.bintrayIvyRepo("evolutiongaming", "sbt-plugins")

val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.5.1")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.7.3")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.7")

addSbtPlugin("com.github.sbt" % "sbt-release" % "1.0.15")

addSbtPlugin("com.evolution" % "sbt-scalac-opts-plugin" % "0.0.9")

addSbtPlugin("com.evolution" % "sbt-artifactory-plugin" % "0.0.2")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.8.1")