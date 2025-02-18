package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.ReviewSchedule

/**
 * An enumeration of the types of updated Review Schedule created that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class ReviewScheduleCreatedTelemetryEventType(
  val value: String,
  val customDimensions: (reviewSchedule: ReviewSchedule) -> Map<String, String>,
) {
  REVIEW_SCHEDULE_CREATED(
    "REVIEW_SCHEDULE_CREATED",
    { updatedReviewSchedule ->
      mapOf(
        "reference" to updatedReviewSchedule.reference.toString(),
        "prisonId" to updatedReviewSchedule.createdAtPrison,
        "userId" to updatedReviewSchedule.createdBy,
      )
    },
  ),
}
