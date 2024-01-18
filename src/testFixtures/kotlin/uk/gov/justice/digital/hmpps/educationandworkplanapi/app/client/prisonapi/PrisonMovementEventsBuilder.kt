package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi

import uk.gov.justice.digital.hmpps.educationandworkplanapi.aValidPrisonNumber
import java.time.LocalDate

fun aValidPrisonMovementEvents(
  prisonNumber: String = aValidPrisonNumber(),
  prisonMovements: Map<Long, List<PrisonMovementEvent>> = mapOf(
    1L to listOf(
      aValidAdmissionPrisonMovementEvent(),
      aValidReleasePrisonMovementEvent(),
      anotherValidAdmissionPrisonMovementEvent(),
      anotherValidReleasePrisonMovementEvent(),
      aValidTransferPrisonMovementEvent(),
      anotherValidTransferPrisonMovementEvent(),
    ),
    2L to listOf(
      anotherValidAdmissionPrisonMovementEvent(),
      anotherValidReleasePrisonMovementEvent(),
    ),
  ),
): PrisonMovementEvents =
  PrisonMovementEvents(
    prisonNumber = prisonNumber,
    prisonMovements = prisonMovements,
  )

fun anotherValidPrisonMovementEvents(
  prisonNumber: String = aValidPrisonNumber(),
  prisonMovements: Map<Long, List<PrisonMovementEvent>> = mapOf(
    2L to listOf(
      anotherValidAdmissionPrisonMovementEvent(),
      anotherValidReleasePrisonMovementEvent(),
    ),
  ),
): PrisonMovementEvents =
  PrisonMovementEvents(
    prisonNumber = prisonNumber,
    prisonMovements = prisonMovements,
  )

fun aValidAdmissionPrisonMovementEvent(
  date: LocalDate = LocalDate.now().minusMonths(6),
  movementType: PrisonMovementType = PrisonMovementType.ADMISSION,
  fromPrisonId: String? = null,
  toPrisonId: String? = "BMI",
): PrisonMovementEvent = PrisonMovementEvent(
  date = date,
  movementType = movementType,
  fromPrisonId = fromPrisonId,
  toPrisonId = toPrisonId,
)

fun anotherValidAdmissionPrisonMovementEvent(
  date: LocalDate = LocalDate.now().minusMonths(1),
  movementType: PrisonMovementType = PrisonMovementType.ADMISSION,
  fromPrisonId: String? = null,
  toPrisonId: String? = "BXI",
): PrisonMovementEvent = PrisonMovementEvent(
  date = date,
  movementType = movementType,
  fromPrisonId = fromPrisonId,
  toPrisonId = toPrisonId,
)

fun aValidReleasePrisonMovementEvent(
  date: LocalDate = LocalDate.now().minusMonths(2),
  movementType: PrisonMovementType = PrisonMovementType.RELEASE,
  fromPrisonId: String? = "BXI",
  toPrisonId: String? = null,
): PrisonMovementEvent = PrisonMovementEvent(
  date = date,
  movementType = movementType,
  fromPrisonId = fromPrisonId,
  toPrisonId = toPrisonId,
)

fun anotherValidReleasePrisonMovementEvent(
  date: LocalDate = LocalDate.now(),
  movementType: PrisonMovementType = PrisonMovementType.RELEASE,
  fromPrisonId: String? = "BXI",
  toPrisonId: String? = null,
): PrisonMovementEvent = PrisonMovementEvent(
  date = date,
  movementType = movementType,
  fromPrisonId = fromPrisonId,
  toPrisonId = toPrisonId,
)

fun aValidTransferPrisonMovementEvent(
  date: LocalDate = LocalDate.now().minusMonths(5),
  movementType: PrisonMovementType = PrisonMovementType.TRANSFER,
  fromPrisonId: String? = "BMI",
  toPrisonId: String? = "MDI",
): PrisonMovementEvent = PrisonMovementEvent(
  date = date,
  movementType = movementType,
  fromPrisonId = fromPrisonId,
  toPrisonId = toPrisonId,
)

fun anotherValidTransferPrisonMovementEvent(
  date: LocalDate = LocalDate.now().minusMonths(1),
  movementType: PrisonMovementType = PrisonMovementType.TRANSFER,
  fromPrisonId: String? = "MDI",
  toPrisonId: String? = "BXI",
): PrisonMovementEvent = PrisonMovementEvent(
  date = date,
  movementType = movementType,
  fromPrisonId = fromPrisonId,
  toPrisonId = toPrisonId,
)
