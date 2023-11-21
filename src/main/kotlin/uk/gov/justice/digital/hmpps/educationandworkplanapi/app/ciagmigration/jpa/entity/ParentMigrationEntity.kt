package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity

/**
 * Abstract class for "parent" entities, which contain one or more collections of [KeyAwareChildMigrationEntity] entities.
 */
abstract class ParentMigrationEntity {

  fun <CHILD : KeyAwareChildMigrationEntity> addChild(newChild: CHILD, existingChildren: MutableList<CHILD>) {
    newChild.associateWithParent(this)
    existingChildren.add(newChild)
  }
}
