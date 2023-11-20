package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.ciagmigration.jpa.entity.induction

/**
 * Abstract class for "parent" entities, which contain one or more collections of [KeyAwareChildMigrationEntity] entities.
 */
abstract class ParentMigrationEntity {

  fun <CHILD : KeyAwareChildMigrationEntity> addChild(newChild: CHILD, existingChildren: MutableList<CHILD>) {
    addChildren(mutableListOf(newChild), existingChildren)
  }

  fun <CHILD : KeyAwareChildMigrationEntity> addChildren(
    newChildren: List<CHILD>,
    existingChildren: MutableList<CHILD>,
  ) {
    newChildren.forEach {
      it.associateWithParent(this)
      existingChildren.add(it)
    }
  }
}
