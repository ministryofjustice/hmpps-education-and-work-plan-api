package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.UpdatedReviewScheduleStatus

/**
 * An enumeration of the types of updated Review Schedule status events that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class UpdatedReviewScheduleStatusTelemetryEventType(
  val value: String,
  val customDimensions: (updatedReviewScheduleStatus: UpdatedReviewScheduleStatus) -> Map<String, String>,
) {
  REVIEW_SCHEDULE_STATUS_UPDATED(
    "REVIEW_SCHEDULE_STATUS_UPDATED",
    { updatedReviewSchedule ->
      mapOf(
        "reference" to updatedReviewSchedule.reference.toString(),
        "prisonId" to updatedReviewSchedule.updatedAtPrison,
        "userId" to updatedReviewSchedule.updatedBy,
      )
    },
  ),
}
