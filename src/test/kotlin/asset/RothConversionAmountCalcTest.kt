package asset

import Amount
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import tax.BracketBasedTaxCalcFixture
import tax.TaxableAmounts
import tax.baseTaxConfigFixture
import yearlyDetailFixture


class RothConversionAmountCalcTest : ShouldSpec({
    val taxableAmount = 100000.0
    val topOfCurrBracket = 120000.0
    val topAmountBelowPct = 200000.0

    val taxableAmounts = TaxableAmounts(person = "Person", fed = taxableAmount)
    val fedTaxCalc = BracketBasedTaxCalcFixture(
        topOfCurrBracket = topOfCurrBracket, topAmountBelowPct = topAmountBelowPct)

    val fedTaxCalcAtTopBracket = BracketBasedTaxCalcFixture(
        topOfCurrBracket = Amount.MAX_VALUE, topAmountBelowPct = Amount.MAX_VALUE)

    val currYear = yearlyDetailFixture()

    should("TilNextBracketRothConv amountToConvert should return top of current fed bracket minus fed taxable ") {
        val taxCalcConfig = baseTaxConfigFixture().copy(fed = fedTaxCalc)
        TilNextBracketRothConv().amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(topOfCurrBracket - taxableAmount)
    }

    should("TilNextBracketRothConv amountToConvert should return 0 is current bracket is top brackt ") {
        val taxCalcConfig = baseTaxConfigFixture().copy(fed = fedTaxCalcAtTopBracket)
        TilNextBracketRothConv().amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(0.0)
    }

    should("MaxTaxRateRothConv amountToConvert should return top of bracket with taxable pct less specifc minus fed taxable") {
        val taxCalcConfig = baseTaxConfigFixture().copy(fed = fedTaxCalc)
        MaxTaxRateRothConv(.25).amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(topAmountBelowPct - taxableAmount)
    }

    should("MaxTaxRateRothConv amountToConvert should return 0 is current bracket is top brackt ") {
        val taxCalcConfig = baseTaxConfigFixture().copy(fed = fedTaxCalcAtTopBracket)
        MaxTaxRateRothConv(.50).amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(0.0)
    }
})

