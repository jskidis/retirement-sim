package progression

import YearMonth
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import util.DateRange
import util.currentDate
import yearlyDetailFixture

class DateRangeAmountAdjusterTest : ShouldSpec({

    val currentYear = currentDate.year
    val rangeEndsThisYear = DateRange(YearMonth(2020, 0), YearMonth(currentYear, 6))
    val rangeStartsThisYear = DateRange(YearMonth(currentYear, 6), YearMonth(2100, 12))

    val unadjustedValue = 1000.0

    should("adjustAmount when range ends this year") {
        val adjuster = DateRangeAmountAdjuster(rangeEndsThisYear)

        adjuster.adjustAmount(unadjustedValue, prevYear = yearlyDetailFixture(currentYear - 2))
            .shouldBe(unadjustedValue) // curr year is last year

        adjuster.adjustAmount(unadjustedValue, prevYear = yearlyDetailFixture(currentYear - 1))
            .shouldBe(unadjustedValue * 0.5) // curr year is this year

        adjuster.adjustAmount(unadjustedValue * 0.5, prevYear = yearlyDetailFixture(currentYear))
            .shouldBe(0.0) // curr year is next year
    }
    should("adjustGapFillValue") {
        val adjuster = DateRangeAmountAdjuster(rangeEndsThisYear)

        adjuster.adjustGapFillValue(unadjustedValue,
            prevYear = yearlyDetailFixture(currentYear - 2)).shouldBe(unadjustedValue)

        adjuster.adjustGapFillValue(unadjustedValue,
            prevYear = yearlyDetailFixture(currentYear - 1)).shouldBe(unadjustedValue * 0.5)

        adjuster.adjustGapFillValue(unadjustedValue * 0.5,
            prevYear = yearlyDetailFixture(currentYear)).shouldBe(0.0)
    }
    should("account for previous year being partial") {
        val adjuster = DateRangeAmountAdjuster(rangeStartsThisYear)

        adjuster.adjustAmount(unadjustedValue, prevYear = yearlyDetailFixture(currentYear - 2))
            .shouldBe(0.0)

        adjuster.adjustAmount(unadjustedValue, prevYear = yearlyDetailFixture(currentYear - 1))
            .shouldBe(unadjustedValue * 0.5)

        adjuster.adjustAmount(unadjustedValue * 0.5, prevYear = yearlyDetailFixture(currentYear))
            .shouldBe(unadjustedValue)
    }
})
