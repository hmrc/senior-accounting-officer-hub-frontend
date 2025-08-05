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
import org.jsoup.nodes.Element
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

  private val testDate = LocalDate.of(2025, 7, 30)

  val companyDetails = CompanyDetails(
    companyName = "Fake Company Ltd",
    referenceId = "fakexxx1234",
    accountingPeriodStartDate = testDate,
    accountingPeriodEndDate = testDate
  )

  val notificationDetails = NotificationDetails(
    dueDate = testDate
  )

  val certificationDetails = CertificationDetails(
    dueDate = testDate
  )
  val doc         = Jsoup.parse(SUT(companyDetails, notificationDetails, certificationDetails).toString)
  val mainContent = doc.getElementById("main-content")

  "HubView" must {

    "must generate a view with the correct heading and title" in {
      val h1 = mainContent.getElementsByTag("h1")
      h1.size() mustBe 1
      h1.get(0).text() mustBe "Senior Accounting Officer notification and certificate account"
      doc.title mustBe "Senior Accounting Officer notification and certificate account - Senior Accounting Officer notification and certificate account - site.govuk"
    }

    "must have correct correct number of sections" in {
      val sections = mainContent.getElementsByAttributeValueContaining("id", "section-")
      sections.size() mustBe 5
    }

    "must have correct content for company details section" in {
      val rows = mainContent.getElementById("section-company-details").select("div.govuk-summary-list__row")
      rows.size() mustBe 3
      validateRow(row = rows.get(0), keyText = "Company name", actionText = "Fake Company Ltd")
      validateRow(row = rows.get(1), keyText = "ReferenceID", actionText = "fakexxx1234")
      validateRow(row = rows.get(2), keyText = "Accounting period", actionText = "30 July 2025 to 30 July 2025")
    }

    "must have correct content for notification section" in {
      val rows = mainContent.getElementById("section-notification").select("div.govuk-summary-list__row")
      rows.size() mustBe 5

      validateRow(
        row = rows.get(0),
        keyText = "Status",
        actionText = "DUE"
      )
      validateRow(row = rows.get(1), keyText = "Due date", actionText = "30 July 2025")
      validateRow(
        row = rows.get(2),
        keyText = "Template",
        actionText = "Download",
        actionHiddenText = Some("the notification template")
      )
      validateRow(
        row = rows.get(3),
        keyText = "Template guidance",
        actionText = "Read",
        actionHiddenText = Some("the notification template guidance")
      )
      validateRow(row = rows.get(4), keyText = "Submission history", actionText = "Not present yet")
    }

    "must have correct content for certification section" in {
      val rows = mainContent.getElementById("section-certification").select("div.govuk-summary-list__row")
      rows.size() mustBe 5

      validateRow(
        row = rows.get(0),
        keyText = "Status",
        actionText = "DUE"
      )
      validateRow(row = rows.get(1), keyText = "Due date", actionText = "30 July 2025")
      validateRow(
        row = rows.get(2),
        keyText = "Template",
        actionText = "Download",
        actionHiddenText = Some("the certification template")
      )
      validateRow(
        row = rows.get(3),
        keyText = "Template guidance",
        actionText = "Read",
        actionHiddenText = Some("the certification template guidance")
      )
      validateRow(row = rows.get(4), keyText = "Submission history", actionText = "Not present yet")
    }

    "must have correct linkText in submit notification link section" in {
      val sectionLink =
        mainContent
          .getElementById("section-submit-notification-link")
          .getElementsByClass("govuk-link")
      sectionLink.size() mustBe 1
      sectionLink.get(0).text() mustBe "Submit a notification"
    }

    "must have correct links and text in final link section" in {
      val sectionLink =
        mainContent
          .getElementById("section-final-links")
          .getElementsByClass("govuk-link")

      sectionLink.size() mustBe 2
      sectionLink.get(0).text() mustBe "Manage contact details"
      sectionLink.get(1).text() mustBe "Manage company details"
    }
  }

  def validateRow(
      row: Element,
      keyText: String,
      actionText: String,
      actionHiddenText: Option[String] = None
  ) = {
    val rowKey    = row.select("dt.govuk-summary-list__key")
    val rowAction = row.select("dd.govuk-summary-list__actions")
    rowKey.size() mustBe 1
    rowAction.size() mustBe 1
    withClue("row keyText mismatch:\n") {
      rowKey.get(0).text() mustBe keyText
    }
    val actionElement = rowAction.get(0)
    actionHiddenText.foreach(hiddenText => {
      val maybeHiddenElement   = Option(actionElement.getElementsByClass("govuk-visually-hidden"))
      val maybeHiddenTextFound = maybeHiddenElement.map(_.text())
      withClue("row actionHiddenText mismatch:\n") {
        maybeHiddenTextFound mustBe Some(hiddenText)
      }
      maybeHiddenElement.map(_.remove())
    })
    val actionTextFound = actionElement.text
    withClue("row actionText mismatch:\n") {
      actionTextFound mustBe actionText
    }
  }
}
