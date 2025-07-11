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

package uk.gov.hmrc.senioraccountingofficerhubfrontend.controllers

import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.senioraccountingofficerhubfrontend.base.SpecBase
import uk.gov.hmrc.senioraccountingofficerhubfrontend.controllers.actions.*
import uk.gov.hmrc.senioraccountingofficerhubfrontend.views.html.UnauthorisedView

class UnauthorisedControllerSpec extends SpecBase {
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .overrides(bind[IdentifierAction].to[FakeIdentifierAction])
      .build()

  private val fakeRequest = FakeRequest("GET", "/")

  private val controller         = app.injector.instanceOf[UnauthorisedController]
  private val view               = app.injector.instanceOf[UnauthorisedView]
  private def messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  "Unauthorised Controller" must {
    "return 200" in {
      val result = controller.onPageLoad()(fakeRequest)
      status(result) mustBe Status.OK
    }

    "return HTML" in {
      val result = controller.onPageLoad()(fakeRequest)
      contentType(result) mustBe Some("text/html")
      charset(result) mustBe Some("utf-8")
      contentAsString(result) mustBe view()(fakeRequest, messages).toString
    }
  }
}
