package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.PreviousQualifications
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.aValidPreviousQualifications
import java.time.Instant
import java.util.UUID

fun aFullyPopulatedInduction(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  workOnRelease: WorkOnRelease = aValidWorkOnRelease(),
  previousQualifications: PreviousQualifications? = aValidPreviousQualifications(),
  previousTraining: PreviousTraining = aValidPreviousTraining(),
  previousWorkExperiences: PreviousWorkExperiences? = aValidPreviousWorkExperiences(),
  inPrisonInterests: InPrisonInterests? = aValidInPrisonInterests(),
  personalSkillsAndInterests: PersonalSkillsAndInterests? = aValidPersonalSkillsAndInterests(),
  futureWorkInterests: FutureWorkInterests? = aValidFutureWorkInterests(),
  createdBy: String? = "asmith_gen",
  createdByDisplayName: String? = "Alex Smith",
  createdAt: Instant? = Instant.now(),
  createdAtPrison: String = "BXI",
  lastUpdatedBy: String? = "bjones_gen",
  lastUpdatedByDisplayName: String? = "Barry Jones",
  lastUpdatedAt: Instant? = Instant.now(),
  lastUpdatedAtPrison: String = "BXI",
) = Induction(
  reference = reference,
  prisonNumber = prisonNumber,
  workOnRelease = workOnRelease,
  previousQualifications = previousQualifications,
  previousTraining = previousTraining,
  previousWorkExperiences = previousWorkExperiences,
  inPrisonInterests = inPrisonInterests,
  personalSkillsAndInterests = personalSkillsAndInterests,
  futureWorkInterests = futureWorkInterests,
  createdBy = createdBy,
  createdByDisplayName = createdByDisplayName,
  createdAt = createdAt,
  createdAtPrison = createdAtPrison,
  lastUpdatedBy = lastUpdatedBy,
  lastUpdatedByDisplayName = lastUpdatedByDisplayName,
  lastUpdatedAt = lastUpdatedAt,
  lastUpdatedAtPrison = lastUpdatedAtPrison,
)
