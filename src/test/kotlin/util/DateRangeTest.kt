package util

import YearMonth
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class DateRangeTest : ShouldSpec({
    should("Determine pct of specified year is in data range") {
        val simpleRange = DateRange(
            start = YearMonth(2024, 0),
            end = YearMonth(2026, 0)
        )
        simpleRange.pctInYear(2023).value.shouldBe(0.0)
        simpleRange.pctInYear(2024).value.shouldBe(1.0)
        simpleRange.pctInYear(2025).value.shouldBe(1.0)
        simpleRange.pctInYear(2026).value.shouldBe(0.0)

        val partialStartRange = DateRange(
            start = YearMonth(2024, 3),
            end = YearMonth(2026, 0)
        )
        partialStartRange.pctInYear(2023).value.shouldBe(0.0)
        partialStartRange.pctInYear(2024).value.shouldBe(0.75)
        partialStartRange.pctInYear(2025).value.shouldBe(1.0)
        partialStartRange.pctInYear(2026).value.shouldBe(0.0)

        val partialEndRange = DateRange(
            start = YearMonth(2024, 0),
            end = YearMonth(2025, 6)
        )
        partialEndRange.pctInYear(2023).value.shouldBe(0.0)
        partialEndRange.pctInYear(2024).value.shouldBe(1.0)
        partialEndRange.pctInYear(2025).value.shouldBe(0.5)
        partialEndRange.pctInYear(2026).value.shouldBe(0.0)

        val bothPartialRange = DateRange(
            start = YearMonth(2024, 3),
            end = YearMonth(2024, 6))
        bothPartialRange.pctInYear(2023).value.shouldBe(0.0)
        bothPartialRange.pctInYear(2024).value.shouldBe(0.25)
        bothPartialRange.pctInYear(2025).value.shouldBe(0.0)
    }
})