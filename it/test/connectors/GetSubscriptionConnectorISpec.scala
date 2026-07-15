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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import connectors.GetSubscriptionConnector
import connectors.GetSubscriptionConnectorISpec.*
import play.api.http.HeaderNames
import support.ISpecBase
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

class GetSubscriptionConnectorISpec extends ISpecBase {

  private val connector = app.injector.instanceOf[GetSubscriptionConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier(authorization = Some(Authorization(testAuthToken)))

  override def additionalConfigs: Map[String, Any] = Map(
    "microservice.services.senior-accounting-officer.host" -> wireMockHost,
    "microservice.services.senior-accounting-officer.port" -> wireMockPort
  )

  "GetSubscriptionConnector" must {
    Seq(200, 401, 403, 500, 502).foreach { expectedStatus =>
      s"return a Future.successful(HttpResponse) for a $expectedStatus response from HIP" in {
        stubFor(
          get(urlEqualTo("/senior-accounting-officer/subscription"))
            .willReturn(
              aResponse()
                .withStatus(expectedStatus)
                .withBody(testResponse)
            )
        )

        val result = connector.getSubscription().futureValue

        result.status mustBe expectedStatus
        if expectedStatus == 204 then result.body mustBe ""
        else result.body mustBe testResponse

        verify(
          1,
          getRequestedFor(urlEqualTo("/senior-accounting-officer/subscription"))
            .withHeader("correlationId", matching(hipCorrelationIdRegex))
            .withHeader(HeaderNames.AUTHORIZATION, equalTo(testAuthToken))
        )
      }
    }

  }
}

object GetSubscriptionConnectorISpec {
  val testAuthToken         = "testAuthToken"
  val testSaoSubscriptionId = "testSaoSubscriptionId"
  val testResponse          = "{}"
  val hipCorrelationIdRegex = "^[0-9a-fA-F]{8}[-][0-9a-fA-F]{4}[-][0-9a-fA-F]{4}[-][0-9a-fA-F]{4}[-][0-9a-fA-F]{12}$"
}
