package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.EntityKeyAware
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.DomainKeyAware

/**
 * Provides a centralised place to update, add or remove existing JPA entities (and thereby apply changes to the
 * database). Manages changes between lists of domain model objects (provided via the REST API) and the equivalent
 * lists of existing JPA entities.
 */
@Component
class InductionEntityListManager<ENTITY : EntityKeyAware, DOMAIN : DomainKeyAware> {

  fun updateExisting(
    existingEntities: MutableList<ENTITY>,
    updatedDomain: List<DOMAIN>,
    mapper: KeyAwareEntityMapper<ENTITY, DOMAIN>,
  ) {
    val updatedDomainKeys = updatedDomain.map { it.key() }
    existingEntities
      .filter { entity -> updatedDomainKeys.contains(entity.key()) }
      .onEach { entity ->
        mapper.updateEntityFromDomain(
          entity,
          updatedDomain.first { dto -> dto.key() == entity.key() },
        )
      }
  }

  fun addNew(
    existingEntities: MutableList<ENTITY>,
    updatedDomain: List<DOMAIN>,
    mapper: KeyAwareEntityMapper<ENTITY, DOMAIN>,
  ) {
    val currentIdentifiers = existingEntities.map { it.key() }

    val newEntities = updatedDomain
      .filter { dto -> !currentIdentifiers.contains(dto.key()) }
      .map { newDto -> mapper.fromDomainToEntity(newDto) }

    if (newEntities.isNotEmpty()) {
      existingEntities.addAll(newEntities)
    }
  }

  fun deleteRemoved(
    existingEntities: MutableList<ENTITY>,
    updatedDomain: List<DOMAIN>,
  ) {
    val updatedIdentifiers = updatedDomain.map { it.key() }

    val removedEntities = existingEntities.filter { entity -> !updatedIdentifiers.contains(entity.key()) }
    if (removedEntities.isNotEmpty()) {
      existingEntities.removeAll(removedEntities)
    }
  }
}