# Play Json tools
[![Build Status](https://github.com/evolution-gaming/play-json-tools/workflows/CI/badge.svg)](https://github.com/evolution-gaming/play-json-tools/actions?query=workflow%3ACI)
[![Coverage Status](https://coveralls.io/repos/github/evolution-gaming/play-json-tools/badge.svg?branch=master)](https://coveralls.io/github/evolution-gaming/play-json-tools?branch=master)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/7be6ba59864a4624917487fab5809573)](https://app.codacy.com/gh/evolution-gaming/play-json-tools/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![Version](https://img.shields.io/badge/version-click-blue)](https://evolution.jfrog.io/artifactory/api/search/latestVersion?g=com.evolution&a=play-json-tools_2.13&repos=public)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellowgreen.svg)](https://opensource.org/licenses/MIT)

1. `play-json-tools` — Set of implicit Play-JSON `Format` helper classes. Example in [FlatFormatSpec](play-json-tools/src/test/scala/com/evolution/playjson/tools/FlatFormatSpec.scala)
2. `play-json-generic` — provides Format derivation for enum like adt's (sealed trait/case objects'). Examples in [EnumerationDerivalSpec](play-json-generic/src/test/scala/com/evolution/playjson/generic/EnumerationDerivalSpec.scala)

   `NestedTypeFormat` names a subtype differently on the two Scala versions. Scala 2 uses lexical
   nesting, Scala 3 uses the sealed hierarchy, so the same subtype can be written as `Inner.Leaf` by
   one and `Leaf` by the other, and neither reads the other's documents.

   The two agree only when the sealed trait is declared at the top level and every subtype sits
   inside that trait's companion object, either directly or inside the companion of a sealed
   sub-trait that is itself declared there. Measured examples of shapes that do **not** agree:

   | subtype | Scala 2 | Scala 3 |
   | --- | --- | --- |
   | declared at the top level | `` (empty) | `Ping` |
   | inside a plain object | `Inner.Leaf` | `Leaf` |
   | under a top-level sealed sub-trait | `Leaf` | `Branch.Leaf` |
   | plain object at the top level | `` (empty) | `com.example.Pulse` |

   Keep to the agreeing shape if documents cross between services built on different Scala versions.

   One name did change in 1.4.0, on Scala 3 only. A plain object whose `toString` was overridden
   with parentheses and contained a `$` was named after the text before that `$`, so an object with
   `override def toString() = "US$99"` was written as `US`; it is now named after its class. Such
   documents no longer read. Overrides without a `$` failed while writing, so `$`-containing ones are
   the only documents of this shape that can exist.
3. `play-json-jsoniter` — provides the fastest way to convert an instance of `play.api.libs.json.JsValue` to byte array and read it back.
   Numbers are written exactly as play-json's JVM serializer writes them, which means a `BigDecimal` keeps its value
   but not necessarily its scale: `100.00` is written as `100`. A number that could not be read back
   under the configured parse limits is rejected with a `JsonWriterException` instead of being
   written.
4. `play-json-circe` — provides conversions to/from `circe` codecs to ease transitions from one library to another. Examples in [CirceToPlayConversionsSpec](play-json-circe/src/test/scala/com/evolution/playjson/circe/CirceToPlayConversionsSpec.scala) and [PlayToCirceConversionsSpec](play-json-circe/src/test/scala/com/evolution/playjson/circe/PlayToCirceConversionsSpec.scala).

All modules are available for 2.13 and 3.

## Setup

Replace `<version>` with the latest release, which the version badge above links to.

```scala
addSbtPlugin("com.evolution" % "sbt-artifactory-plugin" % "0.0.2")

libraryDependencies += "com.evolution" %% "play-json-tools"    % "<version>"
libraryDependencies += "com.evolution" %% "play-json-generic"  % "<version>"
libraryDependencies += "com.evolution" %% "play-json-jsoniter" % "<version>"
libraryDependencies += "com.evolution" %% "play-json-circe"    % "<version>"
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
