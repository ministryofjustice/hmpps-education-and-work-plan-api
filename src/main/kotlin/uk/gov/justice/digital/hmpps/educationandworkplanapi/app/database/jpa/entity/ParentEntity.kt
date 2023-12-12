package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity

/**
 * Interface for "parent" entities, which contain one or more collections of [KeyAwareChildEntity] entities.
 */
interface ParentEntity {

  fun childEntityUpdated()

  fun <CHILD : KeyAwareChildEntity> addChild(newChild: CHILD, existingChildren: MutableList<CHILD>) {
    addChildren(mutableListOf(newChild), existingChildren)
  }

  fun <CHILD : KeyAwareChildEntity> addChildren(newChildren: List<CHILD>, existingChildren: MutableList<CHILD>) {
    newChildren.forEach {
      it.associateWithParent(this)
      existingChildren.add(it)
    }
  }
}
