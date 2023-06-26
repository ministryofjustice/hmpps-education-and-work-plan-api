package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

const val HAS_EDIT_AUTHORITY = """
    hasAuthority("ROLE_EDUCATION_WORK_PLAN_EDITOR")
"""

const val HAS_VIEW_AUTHORITY = """
    hasAnyAuthority("ROLE_EDUCATION_WORK_PLAN_EDITOR", "ROLE_EDUCATION_WORK_PLAN_VIEWER")
"""
