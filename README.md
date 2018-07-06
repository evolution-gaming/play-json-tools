# Play Json tools [![Build Status](https://travis-ci.org/evolution-gaming/play-json-tools.svg)](https://travis-ci.org/evolution-gaming/play-json-tools) [![Coverage Status](https://coveralls.io/repos/evolution-gaming/play-json-tools/badge.svg)](https://coveralls.io/r/evolution-gaming/play-json-tools) [ ![version](https://api.bintray.com/packages/evolutiongaming/maven/play-json-tools/images/download.svg) ](https://bintray.com/evolutiongaming/maven/play-json-tools/_latestVersion) [![License: MIT](https://img.shields.io/badge/License-MIT-yellowgreen.svg)](https://opensource.org/licenses/MIT)

1. play-json-tools - Set of implicit Play-JSON `Format` helper classes. Example in [FlatFormatSpec](play-json-tools/src/test/scala/com/evolutiongaming/util/FlatFormatSpec.scala)
1. play-json-generic - provides Format derivation for enum like adt's (sealed trait/case objects'). Examples in [EnumerationDerivalSpec](play-json-generic/src/test/scala/com/evolutiongaming/util/generic/EnumerationDerivalSpec.scala)

## Setup

```scala
resolvers += Resolver.bintrayRepo("evolutiongaming", "maven")

libraryDependencies += "com.evolutiongaming" %% "play-json-tools" % "0.2.2"
libraryDependencies += "com.evolutiongaming" %% "play-json-generic" % "0.2.2"
```
