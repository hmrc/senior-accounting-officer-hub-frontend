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

package uk.gov.hmrc.senioraccountingofficerhubfrontend.viewmodels.govuk

import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.FormGroup
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.{DateInput, InputItem}
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.senioraccountingofficerhubfrontend.viewmodels.ErrorMessageAwareness

object date extends DateFluency

trait DateFluency {

  object DateViewModel extends ErrorMessageAwareness {

    def apply(
        field: Field,
        legend: Legend
    )(implicit messages: Messages): DateInput =
      apply(
        field = field,
        fieldset = Fieldset(legend = Some(legend))
      )

    def apply(
        field: Field,
        fieldset: Fieldset
    )(implicit messages: Messages): DateInput = {

      val errorClass = "govuk-input--error"

      val dayError         = field.error.exists(_.args.contains(messages("date.error.day")))
      val monthError       = field.error.exists(_.args.contains(messages("date.error.month")))
      val yearError        = field.error.exists(_.args.contains(messages("date.error.year")))
      val anySpecificError = dayError || monthError || yearError
      val allFieldsError   = field.error.isDefined && !anySpecificError

      val dayErrorClass   = if (dayError || allFieldsError) errorClass else ""
      val monthErrorClass = if (monthError || allFieldsError) errorClass else ""
      val yearErrorClass  = if (yearError || allFieldsError) errorClass else ""

      val items = Seq(
        InputItem(
          id = s"${field.id}.day",
          name = s"${field.name}.day",
          value = field("day").value,
          label = Some(messages("date.day")),
          classes = s"govuk-input--width-2 $dayErrorClass".trim
        ),
        InputItem(
          id = s"${field.id}.month",
          name = s"${field.name}.month",
          value = field("month").value,
          label = Some(messages("date.month")),
          classes = s"govuk-input--width-2 $monthErrorClass".trim
        ),
        InputItem(
          id = s"${field.id}.year",
          name = s"${field.name}.year",
          value = field("year").value,
          label = Some(messages("date.year")),
          classes = s"govuk-input--width-4 $yearErrorClass".trim
        )
      )

      DateInput(
        fieldset = Some(fieldset),
        items = items,
        id = field.id,
        errorMessage = errorMessage(field)
      )
    }
  }

  implicit class FluentDate(date: DateInput) {

    def withNamePrefix(prefix: String): DateInput =
      date.copy(namePrefix = Some(prefix))

    def withHint(hint: Hint): DateInput =
      date.copy(hint = Some(hint))

    def withFormGroup(formGroup: FormGroup): DateInput =
      date.copy(formGroup = formGroup)

    def withCssClass(newClass: String): DateInput =
      date.copy(classes = s"${date.classes} $newClass")

    def withAttribute(attribute: (String, String)): DateInput =
      date.copy(attributes = date.attributes + attribute)

    def asDateOfBirth(): DateInput =
      date.copy(items = date.items map { item =>
        val name = item.id.split('.').last
        item.copy(autocomplete = Some(s"bday-$name"))
      })
  }
}
