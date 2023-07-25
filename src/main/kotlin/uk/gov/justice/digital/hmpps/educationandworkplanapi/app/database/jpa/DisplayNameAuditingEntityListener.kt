package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa

import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate

/**
 * JPA Entity Listener that sets fields annotated with [AuditDisplayName] with the authenticated user's display name.
 */
class DisplayNameAuditingEntityListener {

  @PrePersist
  @PreUpdate
  fun updateAuditDisplayNameFields(target: Any) {
    val currentUserDisplayName = UserPrincipalAuditorAware.getCurrentAuditorDisplayName()
    target.javaClass.declaredFields
      .filter { field -> field.isAnnotationPresent(AuditDisplayName::class.java) }
      .onEach { field ->
        field.isAccessible = true
        field.set(target, currentUserDisplayName)
      }
  }

  /**
   * Simple marker annotation to mark an entity field as having its value set by [DisplayNameAuditingEntityListener].
   */
  @Target(AnnotationTarget.FIELD)
  @Retention(AnnotationRetention.RUNTIME)
  @MustBeDocumented
  annotation class AuditDisplayName
}
