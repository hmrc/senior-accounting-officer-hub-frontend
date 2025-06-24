import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.6"

lazy val microservice = Project("senior-accounting-officer-hub-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    pipelineStages := Seq(gzip),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat.*",
      "uk.gov.hmrc.govukfrontend.views.html.components.*",
      "uk.gov.hmrc.hmrcfrontend.views.html.components.*",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers.*",
      "uk.gov.hmrc.hmrcfrontend.views.config.*",
      "uk.gov.hmrc.senioraccountingofficerhubfrontend.controllers",
      "uk.gov.hmrc.senioraccountingofficerhubfrontend.controllers.routes",
      "uk.gov.hmrc.senioraccountingofficerhubfrontend.views.html.*",
      "uk.gov.hmrc.senioraccountingofficerhubfrontend.views.ViewUtils.*",
      "uk.gov.hmrc.senioraccountingofficerhubfrontend.viewmodels.*",
      "uk.gov.hmrc.senioraccountingofficerhubfrontend.viewmodels.govuk.all.*"
    ),
    PlayKeys.playDefaultPort := 10056
  )
  .settings(CodeCoverageSettings.settings *)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    libraryDependencies ++= AppDependencies.it,
    // dependencyOverrides for:
    // "com.github.tomakehurst" % "wiremock" % "3.0.1"
    // Scala module 2.14.3 requires Jackson Databind version >= 2.14.0 and < 2.15.0
    dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.3"
  )
