package util

import currentDate
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe

class PortionOfYearPastTest : ShouldSpec ({
    should("calc returns 0.0 if year is then future, 1.0 if in the past, and some fraction if present year") {
        PortionOfYearPast.calc(2090).shouldBe(0.0)
        PortionOfYearPast.calc(2000).shouldBe(1.0)

        val result = PortionOfYearPast.calc(currentDate.year)
        result.shouldBeLessThan(1.0)
        result.shouldBeGreaterThan(0.0)
    }
})