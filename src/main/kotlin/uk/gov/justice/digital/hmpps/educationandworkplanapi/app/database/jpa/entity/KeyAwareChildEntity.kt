package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

/**
 * Interface for "child" entity classes that can provide a key to help identify them, or potentially to sort them
 * within Collections.
 */
interface KeyAwareChildEntity {
  fun associateWithParent(parent: ParentEntity)

  fun key(): String
}
