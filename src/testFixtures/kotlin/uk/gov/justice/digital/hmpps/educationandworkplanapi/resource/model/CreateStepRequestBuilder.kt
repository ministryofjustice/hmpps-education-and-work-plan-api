package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import java.time.LocalDate

fun aValidCreateStepRequest(
  title: String = "Book communication skills course",
  targetDate: LocalDate? = LocalDate.now().plusMonths(1),
  sequenceNumber: Int = 1,
): CreateStepRequest =
  CreateStepRequest(
    title = title,
    targetDate = targetDate,
    sequenceNumber = sequenceNumber,
  )

fun anotherValidCreateStepRequest(
  title: String = "Complete communication skills course",
  targetDate: LocalDate? = LocalDate.now().plusMonths(6),
  sequenceNumber: Int = 2,
): CreateStepRequest =
  CreateStepRequest(
    title = title,
    targetDate = targetDate,
    sequenceNumber = sequenceNumber,
  )
