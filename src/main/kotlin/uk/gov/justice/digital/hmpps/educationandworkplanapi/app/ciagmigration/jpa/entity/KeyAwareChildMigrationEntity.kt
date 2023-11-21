package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity

/**
 * Interface for "child" entity classes that can provide a key to help identify them, or potentially to sort them
 * within Collections.
 */
interface KeyAwareChildMigrationEntity {
  fun associateWithParent(parent: ParentMigrationEntity)

  fun key(): String
}
