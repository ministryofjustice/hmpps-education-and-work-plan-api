package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.ReviewType
import uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model.SessionType
import java.time.LocalDate

@Service
class ReviewTypeService {
  fun reviewType(releaseDate: LocalDate?, scheduleCalculationRule: String, scheduleStatus: String? = null, followingTransfer: Boolean = false): ReviewType {
    val now = LocalDate.now()
    val threeMonthsFromNow = now.plusMonths(3)

    return when {
      releaseDate != null &&
        !releaseDate.isBefore(now) &&
        !releaseDate.isAfter(threeMonthsFromNow) ->
        ReviewType.PRE_RELEASE_REVIEW

      followingTransfer || scheduleStatus?.contains("TRANSFER") == true || scheduleCalculationRule.contains("TRANSFER") || scheduleCalculationRule.contains("PRISONER_READMISSION") ->
        ReviewType.TRANSFER_REVIEW

      else ->
        ReviewType.REVIEW
    }
  }

  fun mapToSessionType(reviewType: ReviewType): SessionType = when (reviewType) {
    ReviewType.REVIEW -> SessionType.REVIEW
    ReviewType.PRE_RELEASE_REVIEW -> SessionType.PRE_RELEASE_REVIEW
    ReviewType.TRANSFER_REVIEW -> SessionType.TRANSFER_REVIEW
  }
}
