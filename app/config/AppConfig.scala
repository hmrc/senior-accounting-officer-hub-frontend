/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfig @Inject() (servicesConfig: ServicesConfig, config: Configuration) {

  def welshLanguageSupportEnabled: Boolean =
    config.getOptional[Boolean]("features.welsh-language-support").getOrElse(false)

  def host: String = config.get[String]("host")

  private def contactHost                  = config.get[String]("contact-frontend.host")
  private def contactFormServiceIdentifier = config.get[String]("serviceId")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  def loginUrl: String         = config.get[String]("urls.login")
  def loginContinueUrl: String = config.get[String]("urls.loginContinue")
  def signOutUrl: String       = config.get[String]("urls.signOut")

  private def exitSurveyBaseUrl: String = config.get[String]("feedback-frontend.host")
  def exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/$contactFormServiceIdentifier"

  def submissionFrontendHostAndContext: String =
    s"${getValue("senior-accounting-officer-submission-frontend.host")}/senior-accounting-officer/submission"

  private def getValue(key: String): String =
    sys.props
      .get(key)
      .getOrElse(
        config.get[String](key)
      )
}

object AppConfig {
  def setValue(key: String, value: String): Unit =
    sys.props.addOne((key, value)): Unit
}
