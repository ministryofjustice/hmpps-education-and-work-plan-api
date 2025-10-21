package uk.gov.justice.digital.hmpps.domain.learningandworkprogress.review.service

import java.time.LocalDate
import java.time.Period

data class MonthsAndDaysLeftToServe(
  val months: Long,
  val days: Int,
) {
  private fun isExactMonths(value: Long) = months == value && days == 0

  fun isNoMoreThan3Months(): Boolean = months < 3 || isExactMonths(3)
  fun isBetween3MonthsAnd3Months7Days(): Boolean = months == 3L && days in 1..7
  fun isBetween3Months8DaysAnd6Months(): Boolean = ((months == 3L && days >= 8) || months >= 4) && (months < 6 || isExactMonths(6))

  fun isBetween6And12Months(): Boolean = months >= 6 && !isExactMonths(6) && (months < 12 || isExactMonths(12))
  fun isBetween12And60Months(): Boolean = months >= 12 && !isExactMonths(12) && (months < 60 || isExactMonths(60))
  fun isMoreThan60Months(): Boolean = months >= 60 && !isExactMonths(60)
}

interface MonthsAndDaysLeftToServeFactory {
  fun until(releaseDate: LocalDate): MonthsAndDaysLeftToServe
}

fun from(baseReleaseDate: LocalDate) = object : MonthsAndDaysLeftToServeFactory {
  override fun until(releaseDate: LocalDate): MonthsAndDaysLeftToServe {
    val timeLeftToServe = Period.between(baseReleaseDate, releaseDate)
    val monthsLeft = timeLeftToServe.toTotalMonths()
    val remainderDays = timeLeftToServe.minusMonths(monthsLeft).days
    return MonthsAndDaysLeftToServe(monthsLeft, remainderDays)
  }
}
