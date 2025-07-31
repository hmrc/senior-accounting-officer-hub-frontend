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

    "must have correct correct number of sections" in {
      val main     = doc.getElementById("main-content")
      val sections = main.getElementsByAttributeValueContaining("id", "section")
      sections.size() mustBe 5
    }

    "must have correct labels and values in company details section" in {
      val section = doc.getElementById("main-content").getElementById("section-CompanyDetails")
      val labels  = section.getElementsByClass("govuk-summary-list__key")
      val values  = section.getElementsByClass("govuk-summary-list__actions")

      labels.size() mustBe 3
      labels.get(0).text() mustBe "Company name"
      labels.get(1).text() mustBe "ReferenceID"
      labels.get(2).text() mustBe "Accounting period"

      values.size() mustBe 3
      values.get(0).text() mustBe "Fake Company Ltd"
      values.get(1).text() mustBe "fakexxx1234"
      values.get(2).text() mustBe "30 July 2025 to 30 July 2025"
    }

    "must have correct linkText in submit notification link section" in {
      val sectionLink =
        doc
          .getElementById("main-content")
          .getElementById("section-SubmitNotificationLink")
          .getElementsByClass("govuk-link")
      sectionLink.size() mustBe 1
      sectionLink.get(0).text() mustBe "Submit a notification"
    }

    "must have correct heading labels and values in notification details section" in {
      val section = doc.getElementById("main-content").getElementById("section-Notification")

      val heading = section.getElementsByClass("govuk-heading-m")
      val labels  = section.getElementsByClass("govuk-summary-list__key")
      val values  = section.getElementsByClass("govuk-summary-list__actions")

      heading.size() mustBe 1
      heading.get(0).text() mustBe "Notification"

      labels.size() mustBe 5
      labels.get(0).text() mustBe "Status"
      labels.get(1).text() mustBe "Due date"
      labels.get(2).text() mustBe "Template"
      labels.get(3).text() mustBe "Template guidance"
      labels.get(4).text() mustBe "Submission history"

      values.size() mustBe 5
      values.get(0).text() mustBe "DUE"
      values.get(1).text() mustBe "30 July 2025"
      values.get(2).text() mustBe "Download the notification template"
      values.get(3).text() mustBe "Read the notification template guidance"
      values.get(4).text() mustBe "Not present yet"
    }

    "must have correct heading labels and values in certification details section" in {
      val section = doc.getElementById("main-content").getElementById("section-Certification")

      val heading = section.getElementsByClass("govuk-heading-m")
      val labels  = section.getElementsByClass("govuk-summary-list__key")
      val values  = section.getElementsByClass("govuk-summary-list__actions")

      heading.size() mustBe 1
      heading.get(0).text() mustBe "Certification"

      labels.size() mustBe 5
      labels.get(0).text() mustBe "Status"
      labels.get(1).text() mustBe "Due date"
      labels.get(2).text() mustBe "Template"
      labels.get(3).text() mustBe "Template guidance"
      labels.get(4).text() mustBe "Submission history"

      values.size() mustBe 5
      values.get(0).text() mustBe "DUE"
      values.get(1).text() mustBe "30 July 2025"
      values.get(2).text() mustBe "Download the certification template"
      values.get(3).text() mustBe "Read the certification template guidance"
      values.get(4).text() mustBe "Not present yet"
    }

    "must have correct links and text in final link section" in {
      val sectionLink =
        doc
          .getElementById("main-content")
          .getElementById("section-finalLinks")
          .getElementsByClass("govuk-link")
      sectionLink.size() mustBe 2
      sectionLink.get(0).text() mustBe "Manage contact details"
      sectionLink.get(1).text() mustBe "Manage company details"
    }
  }
}
