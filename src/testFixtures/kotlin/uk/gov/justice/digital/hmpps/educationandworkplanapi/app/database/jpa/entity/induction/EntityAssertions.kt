package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.database.jpa.entity.induction

// JPA managed fields, plus the reference field, which are all managed/generated within the API
internal val INTERNALLY_MANAGED_FIELDS =
  arrayOf(
    ".*id",
    ".*reference",
    ".*createdAt",
    ".*createdBy",
    ".*createdByDisplayName",
    ".*updatedAt",
    ".*updatedBy",
    ".*updatedByDisplayName",
    ".*parent",
  )
