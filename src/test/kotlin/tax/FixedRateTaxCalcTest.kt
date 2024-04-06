package tax

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class FixedRateTaxCalcTest : ShouldSpec({
    val taxRate = 0.10
    val income = 1000.0
    val currYear = yearlyDetailFixture()

    should("determineTax") {
        FixedRateTaxCalc(taxRate).determineTax(income, currYear).shouldBe(taxRate * income)
        FixedRateTaxCalc(taxRate).determineTax(-1000.0, currYear).shouldBe(0.0)
    }
})
