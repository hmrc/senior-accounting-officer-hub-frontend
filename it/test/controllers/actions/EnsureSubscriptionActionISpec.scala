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

package controllers.actions

import controllers.actions.EnsureSubscriptionActionISpec.*
import models.{Contact, NominatedCompany, SaoSubscription, UserAnswers}
import org.scalatest.BeforeAndAfterEach
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.DefaultBodyReadables.readableAsString
import play.api.mvc.{RequestHeader, Results}
import play.api.test.FakeRequest
import repositories.SessionRepository
import support.MockAuthHelper.{authSession, testId, testSubscriptionId}
import support.{ISpecBase, MockAuthHelper, MockGetSubscriptionHelper, SessionCookieBaker}
import views.html.ErrorTemplate

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}

class EnsureSubscriptionActionISpec extends ISpecBase with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    MockAuthHelper.mockAuthOk()
    repository.clear(testId).futureValue
  }

  override def applicationBuilder: GuiceApplicationBuilder =
    GuiceApplicationBuilder()
      .overrides(bind[Clock].toInstance(stubClock))
      .appRoutes { app =>
        val identifierAction         = app.injector.instanceOf[IdentifierAction]
        val EnsureSubscriptionAction = app.injector.instanceOf[EnsureSubscriptionAction]

        { case ("GET", testPath) =>
          (identifierAction andThen EnsureSubscriptionAction) { request =>
            Results.Ok(testSuccessBody(request.userId, request.saoSubscriptionId))
          }
        }
      }

  def repository: SessionRepository = app.injector.instanceOf[SessionRepository]

  def targetUrl = s"$baseUrl$testPath"

  "An endpoint with EnsureSubscriptionAction" must {
    "pass the action successfully" when {
      "subscription is already in mongo" in {
        repository.set(userAnswers).futureValue

        val result = wsClient
          .url(targetUrl)
          .withHttpHeaders(
            HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
            "Csrf-Token"       -> "nocheck"
          )
          .get()
          .futureValue

        result.status mustBe 200
        result.body[String] mustBe testSuccessBody(testId, testSubscriptionId)

        MockGetSubscriptionHelper.verifyGetSubscriptionWasCalled(times = 0)
        repository.get(testId).futureValue mustBe Some(userAnswers)
      }

      "subscription is not in mongo, but getSubscription was successful" in {
        repository.get(testId).futureValue mustBe None
        MockGetSubscriptionHelper.mockGetSubscriptionOk()

        val result = wsClient
          .url(targetUrl)
          .withHttpHeaders(
            HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
            "Csrf-Token"       -> "nocheck"
          )
          .get()
          .futureValue

        result.status mustBe 200
        result.body[String] mustBe testSuccessBody(testId, testSubscriptionId)

        MockGetSubscriptionHelper.verifyGetSubscriptionWasCalled()
        repository.get(testId).futureValue mustBe Some(userAnswers)
      }
    }

    "return 500" when {

      "getSubscription returned 200 but with a malformed response" in {
        repository.get(testId).futureValue mustBe None
        MockGetSubscriptionHelper.mockGetSubscriptionOk(body = "{}")

        val result = wsClient
          .url(targetUrl)
          .withHttpHeaders(
            HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
            "Csrf-Token"       -> "nocheck"
          )
          .get()
          .futureValue

        result.status mustBe 500
        result.body[String] mustBe default500ErrorTemplate

        MockGetSubscriptionHelper.verifyGetSubscriptionWasCalled()
        repository.get(testId).futureValue mustBe None
      }

      "getSubscription returned 500" in {
        repository.get(testId).futureValue mustBe None
        MockGetSubscriptionHelper.mockGetSubscription(status = 500, body = "{}")

        val result = wsClient
          .url(targetUrl)
          .withHttpHeaders(
            HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
            "Csrf-Token"       -> "nocheck"
          )
          .get()
          .futureValue

        result.status mustBe 500
        result.body[String] mustBe default500ErrorTemplate

        MockGetSubscriptionHelper.verifyGetSubscriptionWasCalled()
        repository.get(testId).futureValue mustBe None
      }

      "getSubscription returned 502" in {
        repository.get(testId).futureValue mustBe None
        MockGetSubscriptionHelper.mockGetSubscription(status = 502, body = "{}")

        val result = wsClient
          .url(targetUrl)
          .withHttpHeaders(
            HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
            "Csrf-Token"       -> "nocheck"
          )
          .get()
          .futureValue

        result.status mustBe 500
        result.body[String] mustBe default500ErrorTemplate

        MockGetSubscriptionHelper.verifyGetSubscriptionWasCalled()
        repository.get(testId).futureValue mustBe None
      }

      "getSubscription returned an unexpected status" in {
        repository.get(testId).futureValue mustBe None
        MockGetSubscriptionHelper.mockGetSubscription(status = 600, body = "{}")

        val result = wsClient
          .url(targetUrl)
          .withHttpHeaders(
            HeaderNames.COOKIE -> SessionCookieBaker.bakeSessionCookie(authSession),
            "Csrf-Token"       -> "nocheck"
          )
          .get()
          .futureValue

        result.status mustBe 500
        result.body[String] mustBe default500ErrorTemplate

        MockGetSubscriptionHelper.verifyGetSubscriptionWasCalled()
        repository.get(testId).futureValue mustBe None
      }

    }

  }

  def default500ErrorTemplate: String = {
    given Messages      = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)
    given RequestHeader = FakeRequest("GET", testPath)
    val template        = app.injector.instanceOf[ErrorTemplate]
    template(
      Messages("global.error.InternalServerError500.title"),
      Messages("global.error.InternalServerError500.heading"),
      Messages("global.error.InternalServerError500.message")
    ).toString
  }
}

object EnsureSubscriptionActionISpec {
  private val instant          = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  import MockGetSubscriptionHelper.*
  val testPath = "/test-ensure-subscription-action"

  def testSuccessBody(userId: String, saoSubscriptionId: String) =
    s"Action Passed Successfully $userId, $saoSubscriptionId"

  def userAnswers: UserAnswers = UserAnswers(
    _id = testId,
    subscription = SaoSubscription(
      etmpSafeId = etmpSafeId,
      nominatedCompany = NominatedCompany(
        name = companyName,
        crn = companyCrn,
        utr = companyUtr
      ),
      contacts = List(
        Contact(name = contact1Name, email = contact1Email, language = contact1Language, status = contact1Status),
        Contact(name = contact2Name, email = contact2Email, language = contact2Language, status = contact2Status)
      )
    ),
    lastUpdated = stubClock.instant()
  )

}
