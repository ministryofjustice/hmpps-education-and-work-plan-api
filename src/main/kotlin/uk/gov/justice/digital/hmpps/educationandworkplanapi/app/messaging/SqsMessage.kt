package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.messaging

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.UUID

@JsonNaming(value = PropertyNamingStrategies.UpperCamelCaseStrategy::class)
data class SqsMessage(
  val Type: String,
  val Message: String,
  val MessageId: UUID,
)
