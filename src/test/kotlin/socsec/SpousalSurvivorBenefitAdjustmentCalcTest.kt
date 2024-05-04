package socsec

import YearMonth
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe

class SpousalSurvivorBenefitAdjustmentCalcTest : ShouldSpec({

    should("calcBenefitAdjustment post 1955") {
        val birthYM = YearMonth(year = 1960, month = 0)

        val expectedResults: List<Pair<YearMonth, Double>> = listOf(
            Pair(YearMonth(2009, 6), 0.0000),
            Pair(YearMonth(2010, 0), 0.5900),
            Pair(YearMonth(2010, 6), 0.5963),
            Pair(YearMonth(2015, 0), 0.6525),
            Pair(YearMonth(2020, 0), 0.7083),
            Pair(YearMonth(2020, 6), 0.7292),
            Pair(YearMonth(2025, 0), 0.9167),
            Pair(YearMonth(2026, 6), 0.9792),
            Pair(YearMonth(2027, 0), 1.000),
            Pair(YearMonth(2027, 6), 1.000),
        )

        expectedResults.forEach {
            SpousalSurvivorBenefitAdjustmentCalc.calcBenefitAdjustment(birthYM, startYM = it.first)
                .shouldBeWithinPercentageOf(it.second, 0.1)
        }
    }

    should("calcBenefit adjustment for post 1955 should be same pre 1955 if age is 1 less") {
        val post1955birth = YearMonth(year = 1960)
        val pre1955birth = YearMonth(year = 1950)

        (63..68).forEach { age ->
            val postResult = SpousalSurvivorBenefitAdjustmentCalc.calcBenefitAdjustment(
                birthYM = post1955birth, startYM = YearMonth(post1955birth.year + age)
            )
            val preResult = SpousalSurvivorBenefitAdjustmentCalc.calcBenefitAdjustment(
                birthYM = pre1955birth, startYM = YearMonth(pre1955birth.year + age - 1)
            )
            postResult.shouldBe(preResult)
        }
    }
})
