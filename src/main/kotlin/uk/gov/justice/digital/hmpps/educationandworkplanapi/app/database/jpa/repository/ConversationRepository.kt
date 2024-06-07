package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.conversation.ConversationEntity
import java.util.UUID

@Repository
interface ConversationRepository : JpaRepository<ConversationEntity, UUID> {

  fun findByReference(reference: UUID): ConversationEntity?

  fun findByPrisonNumber(prisonNumber: String, pageable: Pageable): Page<ConversationEntity>

  fun findAllByPrisonNumber(prisonNumber: String): List<ConversationEntity>
}
