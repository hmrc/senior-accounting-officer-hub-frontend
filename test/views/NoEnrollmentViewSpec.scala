/*
 * Copyright 2026 HM Revenue & Customs
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

package views

import base.ViewSpecBase
import config.AppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.NoEnrollmentViewSpec.*
import views.html.NoEnrollmentView

class NoEnrollmentViewSpec extends ViewSpecBase[NoEnrollmentView] {

  private lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val doc: Document = Jsoup.parse(SUT().toString)

  "NoEnrollmentView" must {

    doc.createTestsWithStandardPageElements(
      pageTitle = pageTitle,
      pageHeading = pageHeading,
      showIsThisPageNotWorkingProperlyLink = true,
      hasError = true
    )

    doc.createTestsWithParagraphs(Seq(paragraph1))

    doc.getMainContent
      .select("p.govuk-body")
      .get(0)
      .createTestWithLink(registerLinkText, appConfig.registerForServiceUrl)
  }
}

object NoEnrollmentViewSpec {
  val pageHeading      = "You do not have access to this service"
  val pageTitle        = "You do not have access to this service"
  val registerLinkText =
    "register to submit a Senior Accounting Officer notification and certificate service"
  val paragraph1: String = s"You need to $registerLinkText to access this page."
}
