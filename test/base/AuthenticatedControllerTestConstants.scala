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

package base

import models.{NominatedCompany, SaoSubscription, UserAnswers}

trait AuthenticatedControllerTestConstants {
  val userAnswersId: String         = "id"
  val testSaoSubscriptionId: String = "subscriptionId"

  val companyName = "name"
  val companyCrn  = "crn"
  val companyUtr  = "utr"
  val etmpSafeId  = "etmpSafeId"

  val subscription: SaoSubscription = SaoSubscription(
    etmpSafeId = etmpSafeId,
    nominatedCompany = NominatedCompany(companyName, companyUtr, Some(companyCrn)),
    contacts = List.empty
  )

  def userAnswers: UserAnswers = UserAnswers(userAnswersId, subscription)

}

object AuthenticatedControllerTestConstants extends AuthenticatedControllerTestConstants
