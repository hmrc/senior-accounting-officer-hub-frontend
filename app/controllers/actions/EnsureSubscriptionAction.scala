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

package controllers.actions

import connectors.GetSubscriptionConnector
import models.{SaoSubscription, UserAnswers}
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.mvc.ActionTransformer
import repositories.SessionRepository
import requests.{DataRequest, IdentifierRequest}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, InternalServerException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

import java.time.Clock
import javax.inject.Inject

trait EnsureSubscriptionAction extends ActionTransformer[IdentifierRequest, DataRequest]

class EnsureSubscriptionActionImpl @Inject() (
    val sessionRepository: SessionRepository,
    getSubscriptionConnector: GetSubscriptionConnector,
    clock: Clock
)(override implicit val executionContext: ExecutionContext)
    extends EnsureSubscriptionAction
    with Logging {

  override protected def transform[A](request: IdentifierRequest[A]): Future[DataRequest[A]] = {
    given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    for {
      maybeUserAnswers <- sessionRepository.get(request.userId)
      userAnswers      <- maybeUserAnswers match {
        case Some(userAnswers) => Future.successful(userAnswers)
        case _                 => getAndSetSubscription(request)
      }
    } yield DataRequest(request.request, request.userId, request.saoSubscriptionId, userAnswers)
  }

  private def getAndSetSubscription[A](request: IdentifierRequest[A])(using HeaderCarrier): Future[UserAnswers] =
    for {
      saoSubscription <- getSubscription()
      userAnswers = UserAnswers(
        _id = request.userId,
        subscription = saoSubscription,
        lastUpdated = clock.instant()
      )
      _ <- sessionRepository.set(userAnswers)
    } yield userAnswers

  private def getSubscription[A]()(using HeaderCarrier): Future[SaoSubscription] =
    getSubscriptionConnector.getSubscription().map {
      case HttpResponse(OK, body, _) =>
        Try(Json.parse(body).as[SaoSubscription]).fold(
          {
            case NonFatal(_) =>
              logger.warn("[GetSubscription][MalformedResponse]")
              throw new InternalServerException("GetSubscription returned a MalformedResponse")
            case fatal => throw fatal
          },
          identity
        )
      case HttpResponse(INTERNAL_SERVER_ERROR, _, _) =>
        logger.warn(s"[GetSubscription][INTERNAL_SERVER_ERROR]")
        throw new InternalServerException(s"GetSubscription returned 500")
      case HttpResponse(BAD_GATEWAY, _, _) =>
        logger.warn(s"[GetSubscription][BAD_GATEWAY]")
        throw new InternalServerException(s"GetSubscription returned 502")
      case HttpResponse(status, _, _) =>
        logger.warn(s"[GetSubscription][Unknown]status=$status")
        throw new InternalServerException(s"GetSubscription returned an unknown status=$status")
    }

}
