# Benchmarks

JMH benchmarks for `play-json-jsoniter`. The module is not part of the aggregate build, so
`sbt test` and CI never run it. Run them by hand:

```sh
sbt benchmark/Jmh/run                                                            # everything
sbt "benchmark/Jmh/run com.evolution.playjson.jsoniter.JsNumberWriteBenchmark"   # one suite
sbt "benchmark/Jmh/run -prof gc JsNumberWriteBenchmark.integers.*"               # allocation too
```

`-f` forks, `-wi`/`-w` warmup iterations and duration, `-i`/`-r` measurement iterations and
duration, `-rf json -rff out.json` to save results. These override the annotations on the class.

## Results

Variants:

| Variant | What it is |
| --- | --- |
| `pre 1.4` | `play-json-jsoniter` before BigDecimal write parity, `JsNumber` passed straight to jsoniter's `writeVal`, the codec as of `cf23acb` |
| `post 1.4` | `play-json-jsoniter` at 1.4, the `*Current` benchmarks |
| `play-json` | play-json alone, `Json.toBytes` and `Json.parse` |
| `circe` | circe alone, `noSpaces` and `io.circe.parser.parse`, on a circe `Json` |
| `play-circe` | a play-json `JsValue` through `play-json-circe` and circe, conversion included |

Payloads:

| Payload | What it is |
| --- | --- |
| `integers` | 1000 whole numbers |
| `decimals` | 1000 values with two decimal places |
| `trailingZeros` | 1000 values with scale 4 and trailing zeros |
| `document` | the shared `TestData` document, a realistic object with few numbers |

Throughput in ops/s, higher is better. Each cell is the JMH score and its error.

Measured on 2026-08-04:

| | |
| --- | --- |
| Machine | Apple M1 Pro, macOS 26.5.2 |
| JDK | OpenJDK 25.0.3 |
| Scala | 2.13.18 |
| Command | `sbt "benchmark/Jmh/run -f 1 -wi 5 -i 5 -w 2s -r 2s com.evolution.playjson.jsoniter.JsNumber.*Benchmark"` |

Keep these figures as a record. A rerun on another JDK or machine belongs in a section of its own,
so the older numbers stay available for comparison.

### Writing

| Payload | pre 1.4 | post 1.4 | play-json | circe | play-circe |
| --- | ---: | ---: | ---: | ---: | ---: |
| `integers` | 59 808 ± 7 711 | 172 441 ± 4 552 | 5 518 ± 112 | 89 225 ± 1 343 | 11 809 ± 494 |
| `decimals` | 97 730 ± 4 184 | 62 447 ± 2 040 | 10 213 ± 684 | 62 961 ± 1 161 | 12 891 ± 243 |
| `trailingZeros` | 60 098 ± 1 697 | 32 076 ± 330 | 5 052 ± 186 | 66 238 ± 1 919 | 12 959 ± 271 |
| `document` | 342 120 ± 7 766 | 375 279 ± 7 174 | 415 715 ± 16 185 | 392 914 ± 44 219 | 169 168 ± 5 197 |

### Reading

| Payload | pre 1.4 | post 1.4 | play-json | circe | play-circe |
| --- | ---: | ---: | ---: | ---: | ---: |
| `integers` | 48 155 ± 10 057 | 50 448 ± 1 241 | 16 927 ± 2 539 | 52 301 ± 4 326 | 12 185 ± 99 |
| `decimals` | 47 424 ± 365 | 45 610 ± 1 355 | 11 799 ± 89 | 60 821 ± 1 390 | 6 444 ± 154 |
| `trailingZeros` | 41 103 ± 1 240 | 41 577 ± 351 | 12 284 ± 304 | 55 116 ± 5 335 | 6 552 ± 57 |
| `document` | 626 094 ± 10 795 | 640 621 ± 19 294 | 285 091 ± 27 457 | 254 644 ± 4 584 | 124 107 ± 5 423 |

`circe` produces a circe `Json`. Every other column produces a play-json `JsValue`.

The `pre 1.4` codec is not in the repository, so a rerun reproduces every column but that one:
[JsonValueCodecJsValue.scala at cf23acb](https://github.com/evolution-gaming/play-json-tools/blob/cf23acb932f2f79cc06158da58a0480bec0bfd7c/play-json-jsoniter/shared/src/main/scala/play/api/libs/json/JsonValueCodecJsValue.scala).
To measure it again, add that file to the benchmark sources under another name and give each
benchmark a variant using it.
