package tax

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class FixedRateTaxCalcTest : FunSpec({
    val taxRate = 0.10
    val income = 1000.0
    val currYear = yearlyDetailFixture()
    test("determineTax") {
        FixedRateTaxCalc(taxRate).determineTax(income, currYear).shouldBe(taxRate * income)
        FixedRateTaxCalc(taxRate).determineTax(-1000.0, currYear).shouldBe(0.0)
    }
})
