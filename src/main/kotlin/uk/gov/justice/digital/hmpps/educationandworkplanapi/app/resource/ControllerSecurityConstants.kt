package uk.gov.justice.digital.hmpps.educationandworkplanapi.app.resource

// Role Constants
// Note: these first two are legacy roles which will be removed in a future PR
const val EDITOR = "ROLE_EDUCATION_WORK_PLAN_EDITOR"
const val VIEWER = "ROLE_EDUCATION_WORK_PLAN_VIEWER"

const val GOALS_RO = "ROLE_EDUCATION_AND_WORK_PLAN__GOALS__RO"
const val GOALS_RW = "ROLE_EDUCATION_AND_WORK_PLAN__GOALS__RW"
const val INDUCTIONS_RO = "ROLE_EDUCATION_AND_WORK_PLAN__INDUCTIONS__RO"
const val INDUCTIONS_RW = "ROLE_EDUCATION_AND_WORK_PLAN__INDUCTIONS__RW"
const val ACTIONPLANS_RO = "ROLE_EDUCATION_AND_WORK_PLAN__ACTIONPLANS__RO"
const val ACTIONPLANS_RW = "ROLE_EDUCATION_AND_WORK_PLAN__ACTIONPLANS__RW"
const val EDUCATION_RO = "ROLE_EDUCATION_AND_WORK_PLAN__EDUCATION__RO"
const val EDUCATION_RW = "ROLE_EDUCATION_AND_WORK_PLAN__EDUCATION__RW"
const val TIMELINE_RO = "ROLE_EDUCATION_AND_WORK_PLAN__TIMELINE__RO"
const val REVIEWS_RO = "ROLE_EDUCATION_AND_WORK_PLAN__REVIEWS__RO"
const val REVIEWS_RW = "ROLE_EDUCATION_AND_WORK_PLAN__REVIEWS__RW"

// Authority Checks

const val HAS_VIEW_GOALS = """hasAnyAuthority("$EDITOR", "$VIEWER", "$GOALS_RO", "$GOALS_RW")"""
const val HAS_EDIT_GOALS = """hasAnyAuthority("$EDITOR", "$GOALS_RW")"""

const val HAS_VIEW_INDUCTIONS = """hasAnyAuthority("$EDITOR", "$VIEWER", "$INDUCTIONS_RO", "$INDUCTIONS_RW")"""
const val HAS_EDIT_INDUCTIONS = """hasAnyAuthority("$EDITOR", "$INDUCTIONS_RW")"""

const val HAS_VIEW_ACTIONPLANS = """hasAnyAuthority("$EDITOR", "$VIEWER", "$ACTIONPLANS_RO", "$ACTIONPLANS_RW")"""
const val HAS_EDIT_ACTIONPLANS = """hasAnyAuthority("$EDITOR", "$ACTIONPLANS_RW")"""

const val HAS_VIEW_EDUCATION = """hasAnyAuthority("$EDITOR", "$VIEWER", "$EDUCATION_RO", "$EDUCATION_RW")"""
const val HAS_EDIT_EDUCATION = """hasAnyAuthority("$EDITOR", "$EDUCATION_RW")"""

const val HAS_VIEW_TIMELINE = """hasAnyAuthority("$EDITOR", "$VIEWER", "$TIMELINE_RO")"""

const val HAS_VIEW_REVIEWS = """hasAnyAuthority("$EDITOR", "$VIEWER", "$REVIEWS_RO", "$REVIEWS_RW")"""
const val HAS_EDIT_REVIEWS = """hasAnyAuthority("$EDITOR", "$REVIEWS_RW")"""

const val HAS_EDIT_SESSIONS = """hasAnyAuthority("$EDITOR", "$INDUCTIONS_RW", "$REVIEWS_RW")"""

const val HAS_SEARCH_PRISONS = """
    (hasAuthority('$ACTIONPLANS_RO') or hasAuthority('$ACTIONPLANS_RW')) 
    and (hasAuthority('$INDUCTIONS_RO') or hasAuthority('$INDUCTIONS_RW'))
"""
