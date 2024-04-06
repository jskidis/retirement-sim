package asset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import tax.BracketBasedTaxCalcFixture
import tax.TaxableAmounts
import tax.taxConfigFixture
import yearlyDetailFixture


class RothConversionAmountCalcTest : ShouldSpec({
    val taxableAmount = 100000.0
    val topOfCurrBracket = 120000.0
    val topAmountBelowPct = 200000.0

    val taxableAmounts = TaxableAmounts(person = "Person", fed = taxableAmount)
    val fedTaxCalc = BracketBasedTaxCalcFixture(
        topOfCurrBracket = topOfCurrBracket, topAmountBelowPct = topAmountBelowPct)

    val taxCalcConfig = taxConfigFixture().copy(fed = fedTaxCalc)
    val currYear = yearlyDetailFixture()

    should("TilNextBracketRothConv amountToConvert should return top of current fed bracket minus fed taxable ") {
        TilNextBracketRothConv().amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(topOfCurrBracket - taxableAmount)
    }

    should("MaxTaxRateRothConv amountToConvert should return top of bracket with taxable pct less specifc minus fed taxable") {
        MaxTaxRateRothConv(.25).amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(topAmountBelowPct - taxableAmount)
    }
})

