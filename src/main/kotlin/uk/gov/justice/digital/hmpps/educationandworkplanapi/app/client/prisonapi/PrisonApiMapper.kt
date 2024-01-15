package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.client.prisonapi

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.PrisonerInPrisonSummary
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.SignificantMovement
import uk.gov.justice.digital.hmpps.prisonapi.resource.model.TransferDetail

@Component
class PrisonApiMapper {
  fun toPrisonMovementEvents(
    prisonerNumber: String,
    prisonerInPrisonSummary: PrisonerInPrisonSummary,
  ): PrisonMovementEvents {
    val prisonMovements = mutableMapOf<Long, List<PrisonMovementEvent>>()

    prisonerInPrisonSummary.prisonPeriod?.forEach {
      val movementEvents = mutableListOf<PrisonMovementEvent>()
      movementEvents.addAll(buildArrivalsAndReleases(it.movementDates))
      movementEvents.addAll(buildTransfers(it.transfers))
      prisonMovements[it.bookingId!!] = movementEvents
    }

    return PrisonMovementEvents(prisonerNumber, prisonMovements)
  }

  private fun buildArrivalsAndReleases(movements: List<SignificantMovement>): List<PrisonMovementEvent> {
    val arrivalsAndReleases = mutableListOf<PrisonMovementEvent>()
    movements.forEach {
      if (isAdmissionIntoPrison(it)) {
        arrivalsAndReleases.add(toPrisonArrivalEvent(it))
      }
      if (isReleaseFromPrison(it)) {
        arrivalsAndReleases.add(toPrisonReleaseEvent(it))
      }
    }

    return arrivalsAndReleases
  }

  private fun buildTransfers(transfers: List<TransferDetail>): List<PrisonMovementEvent> =
    transfers.map {
      PrisonMovementEvent(
        date = it.dateInToPrison!!.toLocalDate(),
        movementType = PrisonMovementType.TRANSFER,
        fromPrisonId = it.fromPrisonId,
        toPrisonId = it.toPrisonId,
      )
    } ?: emptyList()

  private fun toPrisonArrivalEvent(movement: SignificantMovement): PrisonMovementEvent =
    PrisonMovementEvent(
      date = movement.dateInToPrison!!.toLocalDate(),
      movementType = PrisonMovementType.ARRIVAL,
      fromPrisonId = null,
      toPrisonId = movement.admittedIntoPrisonId,
    )

  private fun toPrisonReleaseEvent(movement: SignificantMovement): PrisonMovementEvent =
    PrisonMovementEvent(
      date = movement.dateOutOfPrison!!.toLocalDate(),
      movementType = PrisonMovementType.RELEASE,
      fromPrisonId = movement.releaseFromPrisonId,
      toPrisonId = null,
    )

  private fun isAdmissionIntoPrison(it: SignificantMovement) =
    it.inwardType == SignificantMovement.InwardType.ADM

  private fun isReleaseFromPrison(it: SignificantMovement) =
    it.outwardType == SignificantMovement.OutwardType.REL
}
