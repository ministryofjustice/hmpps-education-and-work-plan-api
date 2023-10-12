package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction

fun aValidFutureWorkInterests(
  interests: List<WorkInterest> = listOf(aValidWorkInterest()),
) =
  FutureWorkInterests(interests = interests)

fun aValidWorkInterest(
  workType: WorkInterestType = WorkInterestType.CONSTRUCTION,
  workTypeOther: String? = null,
  role: String = "Bricklaying",
) = WorkInterest(
  workType = workType,
  workTypeOther = workTypeOther,
  role = role,
)
