package transfers

import Amount
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import tax.BracketBasedTaxCalcFixture
import tax.BracketCase
import tax.TaxableAmounts
import tax.baseTaxConfigFixture
import yearlyDetailFixture

class MaxTaxRateRothConvTest : ShouldSpec({

    val bracketStart = 200000.0
    val bracketEnd = 300000.0
    val bracketCase = BracketCase(pct = .25, bracketStart, bracketEnd)
    val topBracketCase = bracketCase.copy(end = Amount.MAX_VALUE)

    val fedTaxable = 100000.0
    val fedLTGTaxable = 10000.0
    val taxableAmounts = TaxableAmounts("Person", fedTaxable, fedLTGTaxable)

    val currYear = yearlyDetailFixture()

    should("amountToConvert returns difference between top of bracket with the specific tax rate (or lower) minus taxable amounts") {
        val taxCalcConfig = baseTaxConfigFixture(
            fed = BracketBasedTaxCalcFixture(backetBelowPct = bracketCase))

        val expectedResults = bracketEnd - fedTaxable - fedLTGTaxable
        val amountCalc = MaxTaxRateRothConv(maxPct = .25)
        amountCalc.amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(expectedResults)
    }

    should("amountToConvert uses the Nth pct of the matching bracket as the top calculation") {
        val taxCalcConfig = baseTaxConfigFixture(
            fed = BracketBasedTaxCalcFixture(backetBelowPct = bracketCase))

        val pctOfHighestBracket = 0.25
        val nthPctOfBracket = (bracketEnd - bracketStart) * pctOfHighestBracket + bracketStart
        val expectedResults =  nthPctOfBracket - fedTaxable - fedLTGTaxable
        val amountCalc = MaxTaxRateRothConv(maxPct = .25, pctOfHighestBracket)
        amountCalc.amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(expectedResults)
    }

    should("amountToConvert returns 0 if the bracket is the top bracket (open ended)") {
        val taxCalcConfig = baseTaxConfigFixture(
            fed = BracketBasedTaxCalcFixture(backetBelowPct = topBracketCase))

        val amountCalc = MaxTaxRateRothConv(maxPct = .25)
        amountCalc.amountToConvert(currYear, taxableAmounts, taxCalcConfig).shouldBeZero()
    }
})
