package progression

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe

class LinearDecreasingAmountTest : ShouldSpec({
    val startAmount = 10000.0
    val startYear = 2024
    val numYears = 10

    should("calcAmount: should return approriate amounts") {
        // Before start year should return start amount
        LinearDecreasingAmount.calcAmount(
            year = startYear - 1, startAmount, startYear, numYears)
            .shouldBe(startAmount)

        // At start year should return start amount
        LinearDecreasingAmount.calcAmount(
            year = startYear, startAmount, startYear, numYears)
            .shouldBe(startAmount)

        // At end year should return 0
        LinearDecreasingAmount.calcAmount(
            year = startYear + numYears, startAmount, startYear, numYears)
            .shouldBe(0)

        // After end year should return 0
        LinearDecreasingAmount.calcAmount(
            year = startYear + numYears + 5, startAmount, startYear, numYears)
            .shouldBe(0)

        // After mid-point year should return half start amount
        LinearDecreasingAmount.calcAmount(
            year = startYear + (numYears / 2), startAmount, startYear, numYears)
            .shouldBeWithinPercentageOf(startAmount * .5, 0.001)

        // At 20% of num years should be 80% of start amount
        LinearDecreasingAmount.calcAmount(
            year = startYear + (numYears * 2 / 10), startAmount, startYear, numYears)
            .shouldBeWithinPercentageOf(startAmount * .8, 0.001)

        // At 80% of num years should be 20% of start amount
        LinearDecreasingAmount.calcAmount(
            year = startYear + (numYears * 8 / 10), startAmount, startYear, numYears)
            .shouldBeWithinPercentageOf(startAmount * .2, .001)
    }
})
