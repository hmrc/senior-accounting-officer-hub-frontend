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

package controllers.testOnly

import config.AppConfig
import org.apache.pekko.util.ByteString
import play.api.http.{HeaderNames, MimeTypes}
import play.api.libs.ws.{BodyWritable, InMemoryBody}
import play.api.mvc.{Action, ControllerComponents}
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder, readStreamHttpResponse}
import uk.gov.hmrc.http.{HttpResponse, StringContextOps}

import scala.concurrent.ExecutionContext

import java.net.{URL, URLDecoder}
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class BackendProxyController @Inject() (
    cc: ControllerComponents,
    httpClient: HttpClientV2,
    appConfig: AppConfig
)(using
    ExecutionContext
) extends BackendController(cc) {

  def proxy(path: String): Action[?] = Action(parse.raw).async { implicit request =>
    val headers =
      request.headers.headers.filter((header, _) => headersToKeep.exists(_.toLowerCase == header.toLowerCase))

    val body               = request.body.asBytes().getOrElse(ByteString().empty)
    val requestContentType =
      request.headers.get(HeaderNames.CONTENT_TYPE).getOrElse(MimeTypes.BINARY)

    given BodyWritable[ByteString] =
      BodyWritable(bytes => InMemoryBody(bytes), requestContentType)

    val http: URL => RequestBuilder = request.method match {
      case "GET"  => httpClient.get(_).setHeader(headers*)
      case "POST" =>
        url =>
          httpClient
            .post(url)
            .withBody[ByteString](body)
            .setHeader(headers*)

      case "PUT" =>
        url =>
          httpClient
            .put(url)
            .withBody[ByteString](body)
            .setHeader(headers*)

      case "HEAD" => httpClient.head(_).setHeader(headers*)
    }

    val queryString = Option(request.rawQueryString).filter(_.nonEmpty).fold("")(qs => s"?$qs")
    val targetUrl   = s"${appConfig.backendBaseUrl}/${URLDecoder.decode(path, StandardCharsets.UTF_8)}$queryString"
    http(url"$targetUrl")
      .stream[HttpResponse]
      .map(response =>
        Status(response.status)
          .chunked(response.bodyAsSource)
          .as(response.header(CONTENT_TYPE).fold(MimeTypes.BINARY)(identity))
      )
  }

  private def headersToKeep = Seq(
    CONTENT_TYPE,
    AUTHORIZATION,
    "correlationId",
    "X-Transmitting-System",
    "X-Originating-System",
    "X-Receipt-Date"
  )

}
