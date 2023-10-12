package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

import java.util.UUID

fun aValidInduction(
  reference: UUID = UUID.randomUUID(),
  prisonNumber: String = "A1234AB",
  workOnRelease: WorkOnRelease = aValidWorkOnRelease(),
  previousQualifications: PreviousQualifications = aValidPreviousQualifications(),
  previousTraining: PreviousTraining = aValidPreviousTraining(),
  previousWorkExperiences: PreviousWorkExperiences = aValidPreviousWorkExperiences(),
  inPrisonInterests: InPrisonInterests = aValidInPrisonInterests(),
  personalSkillsAndInterests: PersonalSkillsAndInterests = aValidPersonalSkillsAndInterests(),
  futureWorkInterests: FutureWorkInterests = aValidFutureWorkInterests(),
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
)
