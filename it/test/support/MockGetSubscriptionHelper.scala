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

package support

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.StubMapping

object MockGetSubscriptionHelper {

  val getSubscriptionUri = "/senior-accounting-officer/subscription"

  val etmpSafeId       = "etmpSafeId"
  val companyCrn       = "companyCrn"
  val companyName      = "companyName"
  val companyUtr       = "companyUtr"
  val contact1Name     = "Contact 1 Name"
  val contact1Email    = "1@test.com"
  val contact1Language = "en"
  val contact1Status   = "valid"
  val contact2Name     = "Contact 2 Name"
  val contact2Email    = "2@test.com"
  val contact2Language = "cy"
  val contact2Status   = "unreachable"

  def getSubscription200Body: String =
    s"""
    |{
    |  "etmpSafeId": "$etmpSafeId",
    |  "contacts": [
    |    {
    |      "name": "$contact1Name",
    |      "email": "$contact1Email",
    |      "language": "$contact1Language",
    |      "status": "$contact1Status"
    |    },
    |    {
    |      "name": "$contact2Name",
    |      "email": "$contact2Email",
    |      "language": "$contact2Language",
    |      "status": "$contact2Status"
    |    }
    |  ],
    |  "nominatedCompany": {
    |    "crn": "$companyCrn",
    |    "name": "$companyName",
    |    "utr": "$companyUtr"
    |  }
    |}""".stripMargin

  def mockGetSubscriptionOk(body: String = getSubscription200Body): StubMapping =
    stubFor(
      get(urlEqualTo(getSubscriptionUri))
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/json")
            .withBody(body)
            .withStatus(200)
        )
    )

  def mockGetSubscription(status: Int, body: String): StubMapping =
    stubFor(
      get(urlEqualTo(getSubscriptionUri))
        .willReturn(
          aResponse()
            .withHeader("content-type", "application/json")
            .withBody(body)
            .withStatus(status)
        )
    )

  def verifyGetSubscriptionWasCalled(times: Int = 1): Unit =
    verify(times, getRequestedFor(urlEqualTo(getSubscriptionUri)))

}
