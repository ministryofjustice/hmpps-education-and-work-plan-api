package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

/**
 * Thrown when am Induction does not exist for a given Prisoner.
 */
class InductionNotFoundException(val prisonNumber: String) :
  RuntimeException("Induction not found for prisoner [$prisonNumber]")

/**
 * Thrown when an Attempt is made to create an Induction for a prisoner who already has one.
 */
class InductionAlreadyExistsException(val prisonNumber: String) :
  RuntimeException("An Induction already exists for prisoner $prisonNumber")

/**
 * Thrown when an Attempt is made to create an Induction Schedule for a prisoner who already has one.
 */
class InductionScheduleAlreadyExistsException(val prisonNumber: String) :
  RuntimeException("An Induction Schedule already exists for prisoner $prisonNumber")

class InductionScheduleNotFoundException(val prisonNumber: String) :
  RuntimeException("Induction schedule not found for prisoner [$prisonNumber]")

class InvalidInductionScheduleStatusException(val prisonNumber: String, val fromStatus: InductionScheduleStatus, val toStatus: InductionScheduleStatus) :
  RuntimeException("Invalid Induction Schedule status transition for prisoner [$prisonNumber] status from $fromStatus to $toStatus")
