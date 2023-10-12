package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

fun aValidPreviousWorkExperiences(
  hasWorkBefore: Boolean = true,
  experiences: List<WorkExperience> = listOf(aValidWorkExperience()),
) =
  PreviousWorkExperiences(
    hasWorkBefore = hasWorkBefore,
    experiences = experiences,
  )

fun aValidWorkExperience(
  experienceType: WorkExperienceType = WorkExperienceType.CONSTRUCTION,
  experienceTypeOther: String? = null,
  role: String = "Forklift truck driver",
  details: String? = null,
) =
  WorkExperience(
    experienceType = experienceType,
    experienceTypeOther = experienceTypeOther,
    role = role,
    details = details,
  )
