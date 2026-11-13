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

import base.ViewSpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.compatible.Assertion
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import views.HubViewSpec.*
import views.html.HubView

class HubViewSpec extends ViewSpecBase[HubView] {

  val testSubmissionFrontendHost = "test-host-config"

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure("senior-accounting-officer-submission-frontend.host" -> testSubmissionFrontendHost)
      .build()
  Seq(true, false).foreach { combinedJourneyEnabled =>
    val doc: Document =
      Jsoup.parse(SUT("Fake Company Ltd", "fakexxx1234", combinedJourneyEnabled = combinedJourneyEnabled).toString)

    s"HubView - combined Journey = $combinedJourneyEnabled" must {

      val paragraphs     = if combinedJourneyEnabled then combinedParagraphs else nonCombinedParagraphs
      val cardParagraphs = if combinedJourneyEnabled then cardParagraphsCombined else cardParagraphsNonCombined

      doc.createTestsWithStandardPageElements(
        pageTitle = pageTitle,
        pageHeading = pageHeading,
        showIsThisPageNotWorkingProperlyLink = true,
        hasError = false
      )

      doc.createTestsWithLargeCaption(
        pageCaption
      )

      doc
        .select(linkLocator1)
        .get(0)
        .createTestWithLink(linkText, linkUrl)

      doc
        .select(linkLocator2)
        .get(0)
        .createTestWithLink(linkText2, linkUrl2)

      doc.createTestsWithParagraphs(paragraphs)

      doc.createTestWithAccountHomeCards()

      doc.createTestWithParagraphsWithinAccountHomeCards(cardParagraphs)

      doc.createTestWithSubHeadingsWithinAccountHomeCards(cardSubheadings)

    }
  }

  extension (doc: Document) {
    def createTestWithAccountHomeCards(): Unit = {
      val homeCards = doc.getMainContent.select(".account-home-card")

      "must have 2 account home cards" in {
        homeCards.size() mustBe 2
      }
    }

    def createTestWithParagraphsWithinAccountHomeCards(paragraphs: Seq[String]): Unit = {
      val homeCardParagraphs = doc.getMainContent.select(".account-home-card p")

      "must have 3 paragraphs within account home cards" in {
        homeCardParagraphs.size() mustBe paragraphs.length
      }

      paragraphs.zipWithIndex.foreach((paragraph, i) => {
        s"must have paragraph '$paragraph'" in {
          homeCardParagraphs.get(i).text mustBe paragraph
        }
      })
    }

    def createTestWithSubHeadingsWithinAccountHomeCards(subheadings: Seq[String]): Unit = {
      val homeCardSubheadings = doc.getMainContent.select(".account-home-card h2")

      "must have 2 subheadings within account home cards" in {
        homeCardSubheadings.size() mustBe subheadings.length
      }

      subheadings.zipWithIndex.foreach((subheading, i) => {
        s"must have subheadings '$subheading'" in {
          homeCardSubheadings.get(i).text mustBe subheading
        }
      })
    }
  }
}

object HubViewSpec {
  val pageTitle    = "Senior Accounting Officer notification and certificate"
  val pageHeading  = "Senior Accounting Officer notification and certificate"
  val pageCaption  = "Fake Company Ltd"
  val linkLocator1 = ".account-home-card:nth-of-type(2)"
  val linkLocator2 = ".account-home-card:nth-of-type(3)"
  val linkText     = "Get a submission template"
  val linkText2    = "Make a submission"
  val linkUrl      = "test-host-config/senior-accounting-officer/submission/template/guidance/make-a-submission"
  val linkUrl2     = "test-host-config/senior-accounting-officer/submission"

  private val nonCombinedText = Seq("You can start a new submission. Submit a notification or a certificate.")
  private val combinedText    = Seq(
    "You can start a new submission. Submit a notification, a certificate, or both at the same time."
  )
  private val baseParagraph = Seq(
    "Reference ID: fakexxx1234",
    "Download a template to prepare your submission and read guidance on how to complete and submit the template."
  )

  val nonCombinedParagraphs: Seq[String] = baseParagraph ++ nonCombinedText
  val combinedParagraphs: Seq[String]    = baseParagraph ++ combinedText

  private val cardParagraphBase = Seq(
    "Download a template to prepare your submission and read guidance on how to complete and submit the template."
  )
  val cardParagraphsCombined: Seq[String]    = cardParagraphBase ++ combinedText
  val cardParagraphsNonCombined: Seq[String] = cardParagraphBase ++ nonCombinedText

  val cardSubheadings: Seq[String] = Seq(
    "Get a submission template",
    "Make a submission"
  )
}
