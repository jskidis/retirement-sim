package socsec

import YearMonth
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import socsec.BenefitAdjustmentCalc.calcBenefitAdjustment

class BenefitAdjustmentCalcTest : FunSpec({

    test("calcBenefitAdjustment Post 1954") {
        val birthYM = YearMonth(year = 1960, month = 0)

        val expectedResults: List<Pair<YearMonth, Double>> = listOf(
            Pair(YearMonth(2021, 6), 0.000),
            Pair(YearMonth(2022, 0), 0.700),
            Pair(YearMonth(2022, 6), 0.725),
            Pair(YearMonth(2023, 0), 0.750),
            Pair(YearMonth(2023, 6), 0.775),
            Pair(YearMonth(2024, 0), 0.800),
            Pair(YearMonth(2024, 6), 0.833),
            Pair(YearMonth(2025, 0), 0.867),
            Pair(YearMonth(2025, 6), 0.900),
            Pair(YearMonth(2026, 0), 0.933),
            Pair(YearMonth(2026, 6), 0.967),
            Pair(YearMonth(2027, 0), 1.000),
            Pair(YearMonth(2027, 6), 1.040),
            Pair(YearMonth(2028, 0), 1.080),
            Pair(YearMonth(2028, 6), 1.120),
            Pair(YearMonth(2029, 0), 1.160),
            Pair(YearMonth(2029, 6), 1.200),
            Pair(YearMonth(2030, 0), 1.240),
            Pair(YearMonth(2030, 6), 1.240),
        )

        expectedResults.forEach {
            calcBenefitAdjustment(birthYM, startYM = it.first)
                .shouldBeWithinPercentageOf(it.second, 0.1)
        }
    }

    test("calcBenefitAdjustment Pre 1954") {
        val birthYM = YearMonth(year = 1950, month = 0)

        val expectedResults: List<Pair<YearMonth, Double>> = listOf(
            Pair(YearMonth(2011, 6), 0.000),
            Pair(YearMonth(2012, 0), 0.750),
            Pair(YearMonth(2012, 6), 0.775),
            Pair(YearMonth(2013, 0), 0.800),
            Pair(YearMonth(2013, 6), 0.833),
            Pair(YearMonth(2014, 0), 0.867),
            Pair(YearMonth(2014, 6), 0.900),
            Pair(YearMonth(2015, 0), 0.933),
            Pair(YearMonth(2015, 6), 0.967),
            Pair(YearMonth(2016, 0), 1.000),
            Pair(YearMonth(2016, 6), 1.040),
            Pair(YearMonth(2017, 0), 1.080),
            Pair(YearMonth(2017, 6), 1.120),
            Pair(YearMonth(2018, 0), 1.160),
            Pair(YearMonth(2018, 6), 1.200),
            Pair(YearMonth(2019, 0), 1.240),
            Pair(YearMonth(2019, 6), 1.280),
            Pair(YearMonth(2020, 0), 1.320),
            Pair(YearMonth(2020, 6), 1.320),
        )

        expectedResults.forEach {
            calcBenefitAdjustment(birthYM, startYM = it.first)
                .shouldBeWithinPercentageOf(it.second, 0.1)
        }
    }
})
