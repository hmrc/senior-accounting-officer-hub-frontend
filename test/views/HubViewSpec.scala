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

package views

import base.SpecBase
import models.{CertificationDetails, CompanyDetails, NotificationDetails}
import org.jsoup.Jsoup
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.HubView

import java.time.LocalDate

class HubViewSpec extends SpecBase with GuiceOneAppPerSuite {
  val SUT: HubView = app.injector.instanceOf[HubView]

  given request: Request[_] = FakeRequest()

  given Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  private val strTestDate = "2025-07-30"

  val companyDetails = CompanyDetails(
    companyName = "Fake Company Ltd",
    referenceId = "fakexxx1234",
    accountingPeriodStartDate = LocalDate.parse(strTestDate),
    accountingPeriodEndDate = LocalDate.parse(strTestDate)
  )

  val notificationDetails = NotificationDetails(
    status = "DUE",
    dueDate = LocalDate.parse(strTestDate),
    submissionHistory = "Link (not yet available)"
  )

  val certificationDetails = CertificationDetails(
    status = "DUE",
    dueDate = LocalDate.parse(strTestDate),
    submissionHistory = "Link (not yet available)"
  )
  val doc = Jsoup.parse(SUT(companyDetails, notificationDetails, certificationDetails).toString)

  "HubView" must {
    "must generate a view with the correct heading and title" in {

      val mainContent = doc.getElementById("main-content")
      val h1          = mainContent.getElementsByTag("h1")
      h1.size() mustBe 1
      h1.get(0).text() mustBe "Senior Accounting Officer notification and certificate account"
      doc.title mustBe "Senior Accounting Officer notification and certificate account - senior-accounting-officer-hub-frontend - site.govuk"
    }

    "must generate a view with the correct links texts" in {
      val links = doc.getElementById("main-content").getElementsByClass("govuk-link")
      links.size() mustBe 7
      links.get(0).text() mustBe "Submit a notification"
      links.get(1).text() mustBe "Download the notification template"
      links.get(2).text() mustBe "Read the notification template guidance"
      links.get(3).text() mustBe "Download the certification template"
      links.get(4).text() mustBe "Read the certification template guidance"
      links.get(5).text() mustBe "Manage contact details"
      links.get(6).text() mustBe "Manage company details"
    }

    "must have correct h2 headings" in {
      val headingsMedium = doc.getElementById("main-content").getElementsByClass("govuk-heading-m")
      headingsMedium.size() mustBe 2
      headingsMedium.get(0).text() mustBe "Notification"
      headingsMedium.get(1).text() mustBe "Certification"
    }

    "must have the correct labels" in {
      val labels = doc.getElementById("main-content").getElementsByClass("govuk-summary-list__key")
      labels.size() mustBe 13
      labels.get(0).text() mustBe "Company name"
      labels.get(1).text() mustBe "ReferenceID"
      labels.get(2).text() mustBe "Accounting period"
      labels.get(3).text() mustBe "Status"
      labels.get(4).text() mustBe "Due date"
      labels.get(5).text() mustBe "Template"
      labels.get(6).text() mustBe "Template guidance"
      labels.get(7).text() mustBe "Submission history"
      labels.get(8).text() mustBe "Status"
      labels.get(9).text() mustBe "Due date"
      labels.get(10).text() mustBe "Template"
      labels.get(11).text() mustBe "Template guidance"
      labels.get(12).text() mustBe "Submission history"
    }

    "must have the correct tags" in {
      val tags = doc.getElementById("main-content").getElementsByClass("govuk-tag govuk-tag--red")
      tags.size() mustBe 2
      tags.get(0).text() mustBe "DUE"
      tags.get(1).text() mustBe "DUE"
    }

    "must have the right content from the model data in the right places" in {
      val actions = doc.getElementById("main-content").getElementsByClass("govuk-summary-list__actions")
      actions.size() mustBe 13
      actions.get(0).text() mustBe "Fake Company Ltd"
      actions.get(1).text() mustBe "fakexxx1234"
      actions.get(2).text() mustBe "30 July 2025 to 30 July 2025"
      actions.get(3).text() mustBe "DUE"
      actions.get(4).text() mustBe "30 July 2025"
      actions.get(5).text() mustBe "Download the notification template"
      actions.get(6).text() mustBe "Read the notification template guidance"
      actions.get(7).text() mustBe "Not present yet"
      actions.get(8).text() mustBe "DUE"
      actions.get(9).text() mustBe "30 July 2025"
      actions.get(10).text() mustBe "Download the certification template"
      actions.get(11).text() mustBe "Read the certification template guidance"
      actions.get(12).text() mustBe "Not present yet"
    }
  }
}
