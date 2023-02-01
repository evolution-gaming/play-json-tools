# Play Json tools
[![Build Status](https://github.com/evolution-gaming/play-json-tools/workflows/CI/badge.svg)](https://github.com/evolution-gaming/play-json-tools/actions?query=workflow%3ACI)
[![Coverage Status](https://coveralls.io/repos/github/evolution-gaming/play-json-tools/badge.svg?branch=master)](https://coveralls.io/github/evolution-gaming/play-json-tools?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c158d2f8c65147b18ab0a958876322cf)](https://www.codacy.com/app/evolution-gaming/play-json-tools?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=evolution-gaming/play-json-tools&amp;utm_campaign=Badge_Grade)
[![Version](https://img.shields.io/badge/version-click-blue)](https://evolution.jfrog.io/artifactory/api/search/latestVersion?g=com.evolutiongaming&a=play-json-tools_2.13&repos=public)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellowgreen.svg)](https://opensource.org/licenses/MIT)

1. `play-json-tools` — Set of implicit Play-JSON `Format` helper classes. Example in [FlatFormatSpec](play-json-tools/src/test/scala/com/evolutiongaming/playjsontools/FlatFormatSpec.scala)
2. `play-json-generic` — provides Format derivation for enum like adt's (sealed trait/case objects'). Examples in [EnumerationDerivalSpec](play-json-generic/src/test/scala/com/evolutiongaming/util/generic/EnumerationDerivalSpec.scala)
3. `play-json-jsoniter` — provides the fastest way to convert an instance of `play.api.libs.json.JsValue` to byte array and read it back.
4. `play-json-circe` — provides efficient conversion from `circe` codec to `play-json` codecs. Examples in [CirceToPlayConversionsSpec](play-json-circe/src/test/scala/com/evolutiongaming/util/CirceToPlayConversionsSpec.scala).

## Setup

```scala
addSbtPlugin("com.evolution" % "sbt-artifactory-plugin" % "0.0.2")

libraryDependencies += "com.evolutiongaming" %% "play-json-tools"   % "0.9.0"
libraryDependencies += "com.evolutiongaming" %% "play-json-generic" % "0.9.0"
libraryDependencies += "com.evolutiongaming" %% "play-json-jsoniter" % "0.9.0"
```
