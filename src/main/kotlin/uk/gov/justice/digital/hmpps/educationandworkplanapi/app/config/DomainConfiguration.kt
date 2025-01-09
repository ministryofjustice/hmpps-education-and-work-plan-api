package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service.ConversationEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service.ConversationPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.conversation.service.ConversationService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service.EducationEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service.EducationPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.education.service.EducationService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleDateCalculationService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.induction.service.InductionService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.note.service.NoteService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleEventService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewSchedulePersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewScheduleService
import uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service.ReviewService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanEventService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.ActionPlanService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalEventService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalNotesService
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalPersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.personallearningplan.service.GoalService
import uk.gov.justice.digital.hmpps.domain.timeline.service.PrisonTimelineService
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelinePersistenceAdapter
import uk.gov.justice.digital.hmpps.domain.timeline.service.TimelineService
import uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.JpaNotePersistenceAdapter

/**
 * Configuration class responsible for providing domain bean implementations
 */
@Configuration
class DomainConfiguration {

  @Bean
  fun goalDomainService(
    goalPersistenceAdapter: GoalPersistenceAdapter,
    goalEventService: GoalEventService,
    actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
    goalNotesService: GoalNotesService,
  ): GoalService =
    GoalService(
      goalPersistenceAdapter,
      goalEventService,
      actionPlanPersistenceAdapter,
      goalNotesService,
    )

  @Bean
  fun actionPlanDomainService(
    actionPlanPersistenceAdapter: ActionPlanPersistenceAdapter,
    actionPlanEventService: ActionPlanEventService,
    goalNotesService: GoalNotesService,
  ): ActionPlanService =
    ActionPlanService(actionPlanPersistenceAdapter, actionPlanEventService, goalNotesService)

  @Bean
  fun noteService(
    notePersistenceAdapter: JpaNotePersistenceAdapter,
  ): NoteService =
    NoteService(notePersistenceAdapter)

  @Bean
  fun timelineDomainService(
    timelinePersistenceAdapter: TimelinePersistenceAdapter,
    prisonTimelineService: PrisonTimelineService,
  ): TimelineService =
    TimelineService(timelinePersistenceAdapter, prisonTimelineService)

  @Bean
  fun inductionDomainService(
    inductionPersistenceAdapter: InductionPersistenceAdapter,
    inductionEventService: InductionEventService,
    inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter,
  ): InductionService =
    InductionService(inductionPersistenceAdapter, inductionEventService, inductionSchedulePersistenceAdapter)

  @Bean
  fun inductionScheduleDomainService(
    inductionSchedulePersistenceAdapter: InductionSchedulePersistenceAdapter,
    inductionScheduleEventService: InductionScheduleEventService,
    inductionScheduleDateCalculationService: InductionScheduleDateCalculationService,
  ): InductionScheduleService =
    InductionScheduleService(inductionSchedulePersistenceAdapter, inductionScheduleEventService, inductionScheduleDateCalculationService)

  @Bean
  fun conversationDomainService(
    conversationPersistenceAdapter: ConversationPersistenceAdapter,
    conversationEventService: ConversationEventService,
  ): ConversationService =
    ConversationService(conversationPersistenceAdapter, conversationEventService)

  @Bean
  fun educationDomainService(
    educationPersistenceAdapter: EducationPersistenceAdapter,
    educationEventService: EducationEventService,
  ): EducationService =
    EducationService(educationPersistenceAdapter, educationEventService)

  @Bean
  fun reviewDomainService(
    reviewEventService: ReviewEventService,
    reviewPersistenceAdapter: ReviewPersistenceAdapter,
    reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter,
    reviewScheduleService: ReviewScheduleService,
  ): ReviewService =
    ReviewService(reviewEventService, reviewPersistenceAdapter, reviewSchedulePersistenceAdapter, reviewScheduleService)

  @Bean
  fun reviewScheduleDomainService(
    reviewSchedulePersistenceAdapter: ReviewSchedulePersistenceAdapter,
    reviewScheduleEventService: ReviewScheduleEventService,
  ): ReviewScheduleService =
    ReviewScheduleService(reviewSchedulePersistenceAdapter, reviewScheduleEventService)
}
