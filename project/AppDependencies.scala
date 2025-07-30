import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapVersion        = "9.18.0"
  private val hmrcFrontendPlayVersion = "12.7.0"
  private val hmrcMongoVersion        = "2.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30" % hmrcFrontendPlayVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % hmrcMongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion % Test,
    "org.jsoup"          % "jsoup"                   % "1.20.1"         % Test,
    "org.scalatestplus" %% "scalacheck-1-18"         % "3.2.19.0"
  )

  val it: Seq[ModuleID] = Seq(
    "com.github.tomakehurst" % "wiremock-jre8" % "3.0.1" % Test
  )

  def apply(): Seq[ModuleID] = compile ++ test
}
