package progression

import YearMonth
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import util.DateRange
import yearlyDetailFixture

class DateRangeAmountAdjusterTest : FunSpec({

    val dateRange = DateRange(YearMonth(2020, 0), YearMonth(2025, 6))

    // Adjuster increments prevYear's year value to determine if current year is in date range
    // so a previous year of 2024, would be in the partial range for a date range that ended during 2025
    val fullyInRange = yearlyDetailFixture(2023)
    val partialInRange = yearlyDetailFixture(2024)
    val outOfRange = yearlyDetailFixture(2025)
    val unadjustedValue = 1000.0

    val adjuster = DateRangeAmountAdjuster(dateRange)

    test("adjustAmount") {
        adjuster.adjustAmount(unadjustedValue, fullyInRange).shouldBe(unadjustedValue)
        adjuster.adjustAmount(unadjustedValue, partialInRange).shouldBe(unadjustedValue * 0.5)
        adjuster.adjustAmount(unadjustedValue, outOfRange).shouldBe(0)
    }
    test("adjustGapFillValue") {
        adjuster.adjustGapFillValue(unadjustedValue, fullyInRange).shouldBe(unadjustedValue)
        adjuster.adjustGapFillValue(unadjustedValue, partialInRange).shouldBe(unadjustedValue * 0.5)
        adjuster.adjustGapFillValue(unadjustedValue, outOfRange).shouldBe(0)
    }
})
