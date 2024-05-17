package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction

/**
 * Interface for domain model classes that enables them to provide a key to help identify them, or potentially to sort
 * them within Collections.
 */
interface KeyAwareDomain {
  fun key(): String
}
