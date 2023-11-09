package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.mapper.induction

import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction.EntityKeyAware
import uk.gov.justice.digital.hmpps.educationandworkplanapi.domain.induction.DomainKeyAware

/**
 * Interface for mapping between entity and domain objects that adhere to the 'KeyAware' interfaces.
 */
interface KeyAwareEntityMapper<ENTITY : EntityKeyAware, DOMAIN : DomainKeyAware> {
  fun updateEntityFromDomain(entity: ENTITY, domain: DOMAIN)

  fun fromDomainToEntity(dto: DOMAIN): ENTITY
}
