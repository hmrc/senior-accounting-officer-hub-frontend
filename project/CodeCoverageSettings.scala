import sbt.Setting
import scoverage.ScoverageKeys

object CodeCoverageSettings {

  private val excludedPackages: Seq[String] = Seq(
    "<empty>",
    "Reverse.*",
    ".*handlers.*",
    ".*components.*",
    ".*viewmodels.govuk.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "config.*",
    "prod.*",
    ".*Routes.*",
    ".*views.*",
    ".*viewmodels.*",
    "testOnly.*",
    "testOnlyDoNotUseInAppConf.*"
  )

  val settings: Seq[Setting[_]] = Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 72,
    ScoverageKeys.coverageFailOnMinimum    := true,
    ScoverageKeys.coverageHighlighting     := true
  )
}