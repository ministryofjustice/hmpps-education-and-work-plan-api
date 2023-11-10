package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

/**
 * Interface for entity model classes that enables them to provide a key to help identify them, or potentially to sort
 * them within Collections.
 */
interface EntityKeyAware {

  fun key(): String
}
