package asset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import tax.BracketBasedTaxCalcFixture
import tax.BracketCase
import tax.TaxableAmounts
import tax.baseTaxConfigFixture
import yearlyDetailFixture

class RothConversionAmountCalcTest : ShouldSpec({
    val taxableAmount = 100000.0
    val currBracket = BracketCase(pct = .20, start = 60000.0, 120000.0)
    val bracketBelowPct = BracketCase(pct = .25, start = 120000.0, end = 200000.0)

    val taxableAmounts = TaxableAmounts(person = "Person", fed = taxableAmount)
    val fedTaxCalc = BracketBasedTaxCalcFixture(
        currBracket = currBracket, backetBelowPct = bracketBelowPct)

    val fedTaxCalcAtTopBracket = BracketBasedTaxCalcFixture(
        currBracket = currBracket.copy(end = Double.MAX_VALUE),
        backetBelowPct = bracketBelowPct.copy(end = Double.MAX_VALUE))

    val currYear = yearlyDetailFixture()

    should("TilNextBracketRothConv amountToConvert should return top of current fed bracket minus fed taxable ") {
        val taxCalcConfig = baseTaxConfigFixture().copy(fed = fedTaxCalc)
        TilNextBracketRothConv().amountToConvert(
            currYear,
            taxableAmounts,
            taxCalcConfig)
            .shouldBe(currBracket.end - taxableAmount)
    }

    should("TilNextBracketRothConv amountToConvert should return 0 is current bracket is top bracket ") {
        val taxCalcConfig = baseTaxConfigFixture().copy(fed = fedTaxCalcAtTopBracket)
        TilNextBracketRothConv().amountToConvert(
            currYear,
            taxableAmounts,
            taxCalcConfig)
            .shouldBe(0.0)
    }

    should("MaxTaxRateRothConv amountToConvert should return top of bracket with taxable pct less specific minus fed taxable") {
        val taxCalcConfig = baseTaxConfigFixture().copy(fed = fedTaxCalc)
        MaxTaxRateRothConv(.25)
            .amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(bracketBelowPct.end - taxableAmount)
    }

    should("MaxTaxRateRothConv amountToConvert should return 0 is current bracket is top brackt ") {
        val taxCalcConfig = baseTaxConfigFixture().copy(fed = fedTaxCalcAtTopBracket)
        MaxTaxRateRothConv(.50)
            .amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(0.0)
    }
})

