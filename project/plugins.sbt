externalResolvers += Resolver.bintrayIvyRepo("evolutiongaming", "sbt-plugins")

val scalaJSVersion = Option(System.getenv("SCALAJS_VERSION")).getOrElse("1.18.2")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.3.2")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.3.1")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.3.15")

// This sets the 'version' property based on the git tag during release process to publish the right version
addSbtPlugin("com.github.sbt" % "sbt-dynver" % "5.1.1")

addSbtPlugin("com.evolution" % "sbt-scalac-opts-plugin" % "0.0.9")

addSbtPlugin("com.evolution" % "sbt-artifactory-plugin" % "0.0.2")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "1.1.1")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.4")