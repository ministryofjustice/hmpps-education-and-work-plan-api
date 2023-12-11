package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.KeyAwareChildEntity
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.KeyAwareDomain

/**
 * Interface for mapping between entity and domain objects that adhere to the 'KeyAware' interfaces.
 */
interface KeyAwareEntityMapper<ENTITY : KeyAwareChildEntity, DOMAIN : KeyAwareDomain> {
  fun updateEntityFromDomain(entity: ENTITY, domain: DOMAIN)

  fun fromDomainToEntity(domain: DOMAIN): ENTITY
}
