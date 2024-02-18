package expense

import YearMonth
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe

class AgeBasedAdjustmentTest: ShouldSpec({
    val result60 = AgeBasedAdjustment.calc(YearMonth(1980, 0), 2040)
    val result65 = AgeBasedAdjustment.calc(YearMonth(1980, 0), 2045)
    val result75 = AgeBasedAdjustment.calc(YearMonth(1980, 0), 2055)
    val result85 = AgeBasedAdjustment.calc(YearMonth(1980, 0), 2065)

    should("AgeBasedAdjustment is 1 when age is below 60") {
        result60.shouldBe(1.0)
    }

    should("AgeBasedAdjustment is less than 1 when age is mid-sixties") {
        result65.shouldBeLessThan(1.0)
    }

    should("AgeBasedAdjustment is less at age 75 than 65") {
        result75.shouldBeLessThan(result65)
    }

    should("AgeBasedAdjustment is greater at age 85 than 75") {
        result85.shouldBeGreaterThan(result75)
    }
})