import com.typesafe.tools.mima.plugin.MimaPlugin
import com.typesafe.tools.mima.plugin.MimaPlugin.autoImport._
import sbt.{Def, _}
import sbt.Keys._

object MimaSettingsPlugin extends AutoPlugin {
  override def requires = MimaPlugin
  override def trigger = allRequirements

  override def projectSettings: Seq[Def.Setting[_]] = {
    val versions = Seq(
      "0.3.10",
      "0.3.11",
      "0.3.12",
    )
    Seq(
      mimaPreviousArtifacts := versions
        .map { v => organization.value %% moduleName.value % v }
        .toSet,
    )
  }
}
