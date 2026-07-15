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

package controllers

import base.AuthenticatedControllerSpecBase
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.HubView

class IndexControllerSpec extends AuthenticatedControllerSpecBase {

  given request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")

  "GET /" must {
    "return 200" in {
      val app = applicationBuilder(userAnswers).build()

      running(app) {
        val controller = app.injector.instanceOf[IndexController]

        val result = controller.onPageLoad()(request)

        status(result) mustBe Status.OK
      }
    }

    "return HTML" in {
      val app = applicationBuilder(userAnswers).build()

      running(app) {
        val controller = app.injector.instanceOf[IndexController]

        val result = controller.onPageLoad()(request)

        val view       = app.injector.instanceOf[HubView]
        given Messages = messages(app)

        contentType(result) mustBe Some("text/html")
        charset(result) mustBe Some("utf-8")
        contentAsString(result) mustBe view(companyName, testSaoSubscriptionId).toString
      }
    }
  }
}
