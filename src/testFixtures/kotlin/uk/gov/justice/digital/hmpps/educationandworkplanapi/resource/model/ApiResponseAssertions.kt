package uk.gov.justice.digital.hmpps.educationandworkplanapi.resource.model

import org.assertj.core.api.AbstractObjectAssert

private val IGNORED_FIELDS = arrayOf(
  ".*id",
  ".*reference",
  ".*createdAt",
  ".*createdDateTime",
  ".*updatedAt",
  ".*modifiedDateTime",
)

/**
 * Convenience method to test for equality, ignoring common "metadata" fields and collection order.
 */
fun <ACTUAL> AbstractObjectAssert<*, ACTUAL>.isEquivalentTo(expected: ACTUAL) {
  usingRecursiveComparison()
    .ignoringCollectionOrder()
    .ignoringFieldsMatchingRegexes(*IGNORED_FIELDS)
    .isEqualTo(expected)
}
