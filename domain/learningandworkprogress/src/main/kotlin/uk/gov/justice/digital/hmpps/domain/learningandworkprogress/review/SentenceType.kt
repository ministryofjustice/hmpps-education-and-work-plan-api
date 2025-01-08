package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review

import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType.INDETERMINATE_SENTENCE
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.SentenceType.RECALL

enum class SentenceType {
  RECALL,
  DEAD,
  INDETERMINATE_SENTENCE,
  SENTENCED,
  CONVICTED_UNSENTENCED,
  CIVIL_PRISONER,
  IMMIGRATION_DETAINEE,
  REMAND,
  UNKNOWN,
  OTHER,
}

/**
 * Return the prisoner's effective sentence type.
 * There is an order or precedence:
 *   * If they have the `isIndeterminate` flag set, they are considered INDETERMINATE_SENTENCE, regardless of anything else
 *   * If they have the `isRecall` flag set, they are considered RECALL
 *   * Else return the sentence type from the prisoner record
 */
fun effectiveSentenceType(prisonerSentenceType: SentenceType, prisonerHasIndeterminateFlag: Boolean, prisonerHasRecallFlag: Boolean) =
  when {
    prisonerHasIndeterminateFlag -> INDETERMINATE_SENTENCE
    prisonerHasRecallFlag -> RECALL
    else -> prisonerSentenceType
  }
