package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.actionplan

import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.CreateStepRequest

fun aValidCreateStepRequest(
  title: String = "Book communication skills course",
  sequenceNumber: Int = 1,
): CreateStepRequest = CreateStepRequest(
  title = title,
  sequenceNumber = sequenceNumber,
)

fun anotherValidCreateStepRequest(
  title: String = "Complete communication skills course",
  sequenceNumber: Int = 2,
): CreateStepRequest = CreateStepRequest(
  title = title,
  sequenceNumber = sequenceNumber,
)
