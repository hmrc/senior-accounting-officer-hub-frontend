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

import controllers.actions.IdentifierAction
import models.{CertificationDetails, CompanyDetails, NotificationDetails}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import views.html.HubView

import java.time.LocalDate
import javax.inject.{Inject, Singleton}

@Singleton
class IndexController @Inject() (
    identify: IdentifierAction,
    mcc: MessagesControllerComponents,
    hubView: HubView
) extends FrontendController(mcc)
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = identify { implicit request =>

    val companyDetails = CompanyDetails(
      companyName = "Fake Company Ltd",
      referenceId = "fakexxx1234",
      accountingPeriodStartDate = LocalDate.now(),
      accountingPeriodEndDate = LocalDate.now()
    )

    val notificationDetails = NotificationDetails(
      dueDate = LocalDate.now()
    )

    val certificationDetails = CertificationDetails(
      dueDate = LocalDate.now()
    )

    Ok(hubView(companyDetails, notificationDetails, certificationDetails))
  }

}
