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

package base

import org.jsoup.nodes.{Document, Element}
import org.scalactic.source.Position
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.{BaseScalaTemplate, Format, HtmlFormat}

import scala.reflect.ClassTag

import ViewSpecBase.*

class ViewSpecBase[T <: BaseScalaTemplate[HtmlFormat.Appendable, Format[HtmlFormat.Appendable]]: ClassTag]
    extends SpecBase {
  def SUT: T = app.injector.instanceOf[T]

  given request: Request[?] = FakeRequest()

  given Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  extension (doc: Document) {
    def getMainContent: Element = doc.getElementById("main-content")
  }

  def mustHaveCorrectPageTitle(document: Document, title: String)(using pos: Position): Unit =
    "must generate a view with the correct title" in {
      document.title mustBe s"$title - $expectedServiceName - site.govuk"
    }

  def mustHaveCorrectPageHeading(document: Document, h1: String)(using pos: Position): Unit =
    "must generate a view with the correct page heading" in {
      val actualH1 = document.getMainContent.getElementsByTag("h1")
      withClue("the page must contain only a single <h1>\n") {
        actualH1.size() mustBe 1
      }
      actualH1.get(0).text() mustBe h1
    }

  def mustShowIsThisPageNotWorkingProperlyLink(document: Document)(using pos: Position): Unit =
    "must generate a view with 'Is this page not working properly? (opens in new tab)' link" in {
      val helpLink = document.getMainContent.select("a.govuk-link.hmrc-report-technical-issue")
      withClue(
        "help link not found, both contact-frontend.host and contact-frontend.serviceId must be set in the configs\n"
      ) {
        helpLink.size() mustBe 1
        helpLink.text() mustBe "Is this page not working properly? (opens in new tab)"
      }

      java.net.URI(helpLink.get(0).attribute("href").getValue).getQuery must include(s"service=$expectedServiceId")
    }
}

object ViewSpecBase {
  val expectedServiceName = "Senior Accounting Officer notification and certificate"
  val expectedServiceId   = "senior-accounting-officer-hub-frontend"
}
