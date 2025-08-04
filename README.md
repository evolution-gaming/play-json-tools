# Play Json tools
[![Build Status](https://github.com/evolution-gaming/play-json-tools/workflows/CI/badge.svg)](https://github.com/evolution-gaming/play-json-tools/actions?query=workflow%3ACI)
[![Coverage Status](https://coveralls.io/repos/github/evolution-gaming/play-json-tools/badge.svg?branch=master)](https://coveralls.io/github/evolution-gaming/play-json-tools?branch=master)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/7be6ba59864a4624917487fab5809573)](https://app.codacy.com/gh/evolution-gaming/play-json-tools/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Version](https://img.shields.io/badge/version-click-blue)](https://evolution.jfrog.io/artifactory/api/search/latestVersion?g=com.evolution&a=play-json-tools_2.13&repos=public)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellowgreen.svg)](https://opensource.org/licenses/MIT)

1. `play-json-tools` — Set of implicit Play-JSON `Format` helper classes. Example in [FlatFormatSpec](play-json-tools/src/test/scala/com/evolution/playjson/tools/FlatFormatSpec.scala)
2. `play-json-generic` — provides Format derivation for enum like adt's (sealed trait/case objects'). Examples in [EnumerationDerivalSpec](play-json-generic/src/test/scala/com/evolution/playjson/generic/EnumerationDerivalSpec.scala)
3. `play-json-jsoniter` — provides the fastest way to convert an instance of `play.api.libs.json.JsValue` to byte array and read it back.
4. `play-json-circe` — provides conversions to/from `circe` codecs to ease transitions from one library to another. Examples in [CirceToPlayConversionsSpec](play-json-circe/src/test/scala/com/evolution/playjson/circe/CirceToPlayConversionsSpec.scala) and [PlayToCirceConversionsSpec](play-json-circe/src/test/scala/com/evolution/playjson/circe/PlayToCirceConversionsSpec.scala).

All modules are available for Scala 2.12, 2.13 and 3.

## Setup

```scala
addSbtPlugin("com.evolution" % "sbt-artifactory-plugin" % "0.0.2")

libraryDependencies += "com.evolution" %% "play-json-tools"   % "1.0.0"
libraryDependencies += "com.evolution" %% "play-json-generic" % "1.0.0"
libraryDependencies += "com.evolution" %% "play-json-jsoniter" % "1.0.0"
```

## Release process
The release process is based on Git tags and makes use of [evolution-gaming/scala-github-actions](https://github.com/evolution-gaming/scala-github-actions) which uses [sbt-dynver](https://github.com/sbt/sbt-dynver) to automatically obtain the version from the latest Git tag. The flow is defined in `.github/workflows/release.yml`.  
A typical release process is as follows:
1. Create and push a new Git tag. The version should be in the format `vX.Y.Z` (example: `v4.1.0`). Example: `git tag v4.1.0 && git push origin v4.1.0`
2. On success, a new GitHub release is automatically created with a calculated diff and auto-generated release notes. 
You can see it on `Releases` page, change the description if needed
3. On failure, the tag is deleted from the remote repository. Please note that your local tag isn't deleted, so if the failure 
is recoverable then you can delete the local tag and try again (an example of *unrecoverable* failure is successfully 
publishing only a few of the artifacts to Artifactory which means a new attempt would fail since Artifactory doesn't allow 
overwriting its contents)
