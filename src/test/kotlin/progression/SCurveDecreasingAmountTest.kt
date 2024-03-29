package progression

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import util.currentDate

class SCurveDecreasingAmountTest : ShouldSpec({
    val startAmount = 10000.0
    val startYear = currentDate.year + 1
    val numYears = 10

    should("calcAmount returns appropriate amounts") {
        // Before start year should return start amount
        SCurveDecreasingAmount.calcAmount(
            year =startYear - 1, startAmount, startYear, numYears)
            .shouldBe(startAmount)

        // At start year should return start amount
        SCurveDecreasingAmount.calcAmount(
            year = startYear, startAmount, startYear, numYears)
            .shouldBe(startAmount)

        // At end year should return 0
        SCurveDecreasingAmount.calcAmount(
            year = startYear + numYears, startAmount, startYear, numYears)
            .shouldBe(0)

        // After end year should return 0
        SCurveDecreasingAmount.calcAmount(
            year = startYear + numYears + 5, startAmount, startYear, numYears)
            .shouldBe(0)

        // After mid-point year should return half start amount
        SCurveDecreasingAmount.calcAmount(
            year = startYear + (numYears / 2), startAmount, startYear, numYears)
            .shouldBeWithinPercentageOf(startAmount * .5, 0.001)

        // At 20% of num years should be greater than 80% of start amount
        SCurveDecreasingAmount.calcAmount(
            year = startYear + (numYears * 2 / 10), startAmount, startYear, numYears)
            .shouldBeGreaterThan(startAmount * .8)

        // At 80% of num years should be less than 20% of start amount
        SCurveDecreasingAmount.calcAmount(
            year = startYear + (numYears * 8 / 10), startAmount, startYear, numYears)
            .shouldBeLessThan(startAmount * .2)
    }
})
