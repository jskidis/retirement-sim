package socsec

import YearMonth
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe

class SpousalBenefitAdjustmentCalcTest : ShouldSpec({

    should("calcBenefitAdjustment post 1955") {
        val birthYM = YearMonth(year = 1960, month = 0)

        val expectedResults: List<Pair<YearMonth, Double>> = listOf(
            Pair(YearMonth(2021, 6), 0.0000),
            Pair(YearMonth(2022, 0), 0.3250),
            Pair(YearMonth(2022, 6), 0.3375),
            Pair(YearMonth(2023, 0), 0.3500),
            Pair(YearMonth(2023, 6), 0.3625),
            Pair(YearMonth(2024, 0), 0.3750),
            Pair(YearMonth(2024, 6), 0.3958),
            Pair(YearMonth(2025, 0), 0.4167),
            Pair(YearMonth(2025, 6), 0.4375),
            Pair(YearMonth(2026, 0), 0.4583),
            Pair(YearMonth(2026, 6), 0.4792),
            Pair(YearMonth(2027, 0), 0.500),
            Pair(YearMonth(2027, 6), 0.500),
        )

        expectedResults.forEach {
            SpousalBenefitAdjustmentCalc.calcBenefitAdjustment(birthYM, startYM = it.first)
                .shouldBeWithinPercentageOf(it.second, 0.1)
        }
    }

    should("calcBenefit adjustment for post 1955 should be same pre 1955 if age is 1 less") {
        val post1955birth = YearMonth(year = 1960)
        val pre1955birth = YearMonth(year = 1950)

        (63..68).forEach {age ->
            val postResult = SpousalBenefitAdjustmentCalc.calcBenefitAdjustment(
                birthYM = post1955birth, startYM = YearMonth(post1955birth.year + age)
            )
            val preResult = SpousalBenefitAdjustmentCalc.calcBenefitAdjustment(
                birthYM = pre1955birth, startYM = YearMonth(pre1955birth.year + age - 1)
            )
            postResult.shouldBe(preResult)
        }
    }
})
