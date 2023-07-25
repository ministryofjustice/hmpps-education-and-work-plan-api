package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.lang.reflect.Field

/**
 * JPA Entity Listener that sets fields annotated with [CreatedByDisplayName] with the authenticated user's display name.
 */
class DisplayNameAuditingEntityListener {

  /**
   * Entity lifecycle hook to update fields annotated with [CreatedByDisplayName] or [LastModifiedByDisplayName]
   * on new entity creation (pre-persist)
   */
  @PrePersist
  fun updateAuditDisplayNameFieldsOnEntityCreate(target: Any) {
    val currentUserDisplayName = UserPrincipalAuditorAware.getCurrentAuditorDisplayName()
    target.javaClass.declaredFields
      .filter { field -> field.shouldBeUpdatedOnEntityCreate() }
      .onEach { field ->
        field.isAccessible = true
        field.set(target, currentUserDisplayName)
      }
  }

  /**
   * Entity lifecycle hook to update fields annotated with [LastModifiedByDisplayName]
   * on entity update (pre-update)
   */
  @PreUpdate
  fun updateAuditDisplayNameFieldsOnEntityUpdate(target: Any) {
    val currentUserDisplayName = UserPrincipalAuditorAware.getCurrentAuditorDisplayName()
    target.javaClass.declaredFields
      .filter { field -> field.shouldBeUpdatedOnEntityUpdate() }
      .onEach { field ->
        field.isAccessible = true
        field.set(target, currentUserDisplayName)
      }
  }

  private fun Field.shouldBeUpdatedOnEntityCreate(): Boolean =
    this.isAnnotationPresent(CreatedByDisplayName::class.java) || this.isAnnotationPresent(LastModifiedByDisplayName::class.java)

  private fun Field.shouldBeUpdatedOnEntityUpdate(): Boolean =
    this.isAnnotationPresent(LastModifiedByDisplayName::class.java)

  /**
   * Simple marker annotation to mark an entity field as containing the display name of the user who created the entity.
   */
  @Target(AnnotationTarget.FIELD)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class CreatedByDisplayName

  /**
   * Simple marker annotation to mark an entity field as containing the display name of the user who last modified the entity.
   */
  @Target(AnnotationTarget.FIELD)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class LastModifiedByDisplayName
}
