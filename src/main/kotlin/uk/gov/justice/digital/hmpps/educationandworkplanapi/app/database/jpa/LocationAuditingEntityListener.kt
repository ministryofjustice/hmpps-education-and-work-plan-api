package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import java.lang.reflect.Field

/**
 * JPA Entity Listener that sets fields annotated with [CreatedLocation] or [LastModifiedLocation]] with the authenticated
 * user's location, typically the prison in which they are working taken from their active caseload ID.
 */
class LocationAuditingEntityListener {

  /**
   * Entity lifecycle hook to update fields annotated with [CreatedLocation] or [LastModifiedLocation]
   * on new entity creation (pre-persist)
   */
  @PrePersist
  fun updateAuditDisplayNameFieldsOnEntityCreate(target: Any) {
    val currentUserLocation = UserPrincipalAuditorAware.getCurrentAuditorLocation()
    target.javaClass.declaredFields
      .filter { field -> field.shouldBeUpdatedOnEntityCreate() }
      .onEach { field ->
        field.isAccessible = true
        field.set(target, currentUserLocation)
      }
  }

  /**
   * Entity lifecycle hook to update fields annotated with [LastModifiedLocation]
   * on entity update (pre-update)
   */
  @PreUpdate
  fun updateAuditDisplayNameFieldsOnEntityUpdate(target: Any) {
    val currentUserLocation = UserPrincipalAuditorAware.getCurrentAuditorLocation()
    target.javaClass.declaredFields
      .filter { field -> field.shouldBeUpdatedOnEntityUpdate() }
      .onEach { field ->
        field.isAccessible = true
        field.set(target, currentUserLocation)
      }
  }

  private fun Field.shouldBeUpdatedOnEntityCreate(): Boolean =
    this.isAnnotationPresent(CreatedLocation::class.java) || this.isAnnotationPresent(LastModifiedLocation::class.java)

  private fun Field.shouldBeUpdatedOnEntityUpdate(): Boolean =
    this.isAnnotationPresent(LastModifiedLocation::class.java)

  /**
   * Simple marker annotation to mark an entity field as containing the location of the user who created the entity.
   */
  @Target(AnnotationTarget.FIELD)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class CreatedLocation

  /**
   * Simple marker annotation to mark an entity field as containing the location of the user who last modified the entity.
   */
  @Target(AnnotationTarget.FIELD)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class LastModifiedLocation
}
