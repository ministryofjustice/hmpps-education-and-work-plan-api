package uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.timeline

/**
 * A [Link] represents a link to the source event in the source system for a given [TimelineEvent].
 *
 * For example a given [TimelineEvent] may be pertaining to something that happened in Application-A; but it may be
 * that Application-B is retrieving and processing the [Timeline]. The [Link] can be used to provide a link from
 * Application-B back to the specific event in Application-A
 *
 * When we talk about 'links' we typically think of web anchor tag links, and it is acknowledged that will be the
 * primary use case.
 * The domain is meant to be the business domain and abstracted from any technology or presentation layer. The concept
 * of a 'link' is difficult to model without thinking about web presentations, and certainly that is how the business
 * think about it. The field `location` is deliberately named to abstract away from a web href, but it is acknowledged
 * this will be the primary use case.
 */
data class Link(
  val title: String,
  val location: String,
)
