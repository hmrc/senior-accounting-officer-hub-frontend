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

import org.apache.pekko.util.ccompat.JavaConverters.ListHasAsScala
import org.jsoup.nodes.{Document, Element}
import org.scalactic.source.Position
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.twirl.api.{BaseScalaTemplate, Format, HtmlFormat}

import scala.reflect.ClassTag
import scala.util.Try

import ViewSpecBase.*

class ViewSpecBase[T <: BaseScalaTemplate[HtmlFormat.Appendable, Format[HtmlFormat.Appendable]]: ClassTag]
    extends SpecBase {
  def SUT: T = app.injector.instanceOf[T]

  given request: Request[?] = FakeRequest()

  given Messages = app.injector.instanceOf[MessagesApi].preferred(request)

  extension (doc: Document) {
    def getMainContent: Element = doc.getElementById("main-content")

    def createTestsWithStandardPageElements(
        pageTitle: String,
        pageHeading: String,
        showIsThisPageNotWorkingProperlyLink: true,
        hasError: Boolean
    )(using pos: Position): Unit = {
      createTestWithPageTitle(pageTitle = pageTitle, hasError = hasError)
      createTestWithPageHeading(pageHeading = pageHeading)
      createTestWithIsThisPageNotWorkingProperlyLink
    }

    def createTestWithPageTitle(pageTitle: String, hasError: Boolean)(using pos: Position): Unit =
      "must generate a view with the correct title" in {
        val errorPrefix = if hasError then "Error: " else ""
        doc.title mustBe s"$errorPrefix$pageTitle - $expectedServiceName - site.govuk"
      }

    def createTestWithPageHeading(pageHeading: String)(using
        pos: Position
    ): Unit =
      "must generate a view with the correct page heading" in {
        val actualH1 = doc.getMainContent.getElementsByTag("h1")
        withClue(s"the page must contain only a single <h1> with content '$pageHeading'\n") {
          actualH1.get(0).text() mustBe pageHeading
          actualH1.size() mustBe 1
        }
      }

    def createTestWithIsThisPageNotWorkingProperlyLink(using
        pos: Position
    ): Unit =
      "must generate a view with 'Is this page not working properly? (opens in new tab)' " in {
        val helpLink = doc.getMainContent.select("a.govuk-link.hmrc-report-technical-issue")
        withClue(
          "help link not found, both contact-frontend.host and contact-frontend.serviceId must be set in the configs\n"
        ) {
          helpLink.text() mustBe "Is this page not working properly? (opens in new tab)"
          helpLink.size() mustBe 1
        }

        java.net.URI(helpLink.get(0).attributes.get("href")).getQuery must include(s"service=$expectedServiceId")
      }
  }
  extension (target: => Document | Element) {
    private def resolve: Element = target match {
      case doc: Document => doc.getMainContent
      case _             => target
    }

    def createTestsWithLargeCaption(
        caption: String
    )(using pos: Position): Unit = {
      createTestWithCountOfElement(
        selector = "span.govuk-caption-l",
        count = 1,
        description = "captions"
      )
      createTestsWithOrderOfElements(
        selector = "span.govuk-caption-l",
        texts = Seq(caption),
        description = "captions"
      )
    }

    private def createTestWithCountOfElement(
        selector: String,
        count: Int,
        description: String
    )(using pos: Position): Unit =
      s"must have $count of $description" in {
        val elements = target.resolve.select(selector).asScala
        withClue(s"Expected $count $description but found ${elements.size}\n") {
          elements.size mustBe count
        }
      }

    private def createTestsWithOrderOfElements(
        selector: String,
        texts: Seq[String],
        description: String
    )(using pos: Position): Unit =
      texts.zipWithIndex.foreach { case (expectedText, index) =>
        s"must have a $description with content '$expectedText' (check ${index + 1})" in {
          val elements = target.resolve.select(selector).asScala

          withClue(s"$description with content '$expectedText' not found\n") {
            val element =
              Try(elements(index))
                .getOrElse(fail(s"Index $index out of bounds for length ${elements.size}"))
            element.text() mustEqual expectedText
          }
        }
      }

    def createTestWithLink(
        linkText: String,
        destinationUrl: String
    )(using
        pos: Position
    ): Unit =
      s"must have expected link with correct text: $linkText and correct url $destinationUrl within provided element" in {
        val element       = target.resolve
        val link: Element = if element.tagName() == "a" then {
          element
        } else {
          val links = element.select("a").asScala
          withClue(s"Expected to find exactly one link in the element but found ${links.size}\n") {
            links.size mustBe 1
          }
          links.head
        }
        withClue(s"link text was not as expected. Got ${link.text()}, expected '$linkText'\n") {
          link.text mustBe linkText
        }
        withClue(s"link href was not as expected. Got ${link.attr("href")}, expected '$destinationUrl'\n") {
          link.attr("href") mustBe destinationUrl
        }

        withClue(s"link must have expected CSS class\n") {
          link.className() must include("govuk-link")
        }
      }

    def createTestsWithParagraphs(
        paragraphs: Seq[String]
    )(using
        pos: Position
    ): Unit = {
      createTestWithCountOfElement(
        selector = excludeHelpLinkAndErrorMessageParagraphsSelector,
        count = paragraphs.size,
        description = "paragraphs"
      )
      createTestsWithOrderOfElements(
        selector = excludeHelpLinkAndErrorMessageParagraphsSelector,
        texts = paragraphs,
        description = "paragraphs"
      )

      "all paragraphs must have the expected CSS class" in {
        def paragraphs =
          target.resolve.select(excludeHelpLinkAndErrorMessageParagraphsSelector).asScala

        paragraphs.foreach(paragraph =>
          withClue(s"$paragraph did not have the expected CSS class\n") {
            paragraph.className() must include("govuk-body")
          }
        )
      }
    }
  }
}

object ViewSpecBase {
  val expectedServiceName = "Senior Accounting Officer notification and certificate"
  val expectedServiceId   = "senior-accounting-officer-hub-frontend"

  val excludeHelpLinkAndErrorMessageParagraphsSelector =
    "p:not(:has(a.hmrc-report-technical-issue), .govuk-error-message)"
}
