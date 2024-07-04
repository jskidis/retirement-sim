package util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe

class PortionOfCurrYearTest : ShouldSpec ({
    should("calc returns 0.0 if year is not current year, and some fraction if present year") {
        PortionOfCurrYear.calc(2090).shouldBe(0.0)
        PortionOfCurrYear.calc(2000).shouldBe(0.0)

        val result = PortionOfCurrYear.calc(currentDate.year)
        result.shouldBeLessThan(1.0)
        result.shouldBeGreaterThan(0.0)
    }
})