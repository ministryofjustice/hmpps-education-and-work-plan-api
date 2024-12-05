package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.service

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.CompletedReview

/**
 * An enumeration of the types of Completed Review events that can be sent to the Telemetry Service, where each item has:
 *   * a text field `value` that is the name of the customEvent recorded in App Insights.
 *   * a function `customDimensions` that returns the customDimensions that are recorded with the App Insights event.
 */
enum class CompletedReviewTelemetryEventType(
  val value: String,
  val customDimensions: (completedReview: CompletedReview) -> Map<String, String>,
) {
  REVIEW_COMPLETED(
    "REVIEW_COMPLETED",
    { completedReview ->
      mapOf(
        "reference" to completedReview.reference.toString(),
        "prisonId" to completedReview.createdAtPrison,
        "userId" to completedReview.createdBy,
      )
    },
  ),
}
