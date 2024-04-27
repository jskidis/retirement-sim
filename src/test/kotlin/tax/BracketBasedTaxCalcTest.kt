package tax

import inflation.CmpdInflationProvider
import inflation.InflationRAC
import inflation.InflationRec
import inflation.StdCmpdInflationProvider
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class BracketBasedTaxCalcTest : ShouldSpec({
    val brackets = listOf(
        TaxBracket(.1, BracketCase(.1, 1000.0, 2000.0), BracketCase(), BracketCase()),
        TaxBracket(.2, BracketCase(.2, 2000.0, 4000.0), BracketCase(), BracketCase()),
        TaxBracket(.4, BracketCase(.4, 4000.0), BracketCase(), BracketCase())
    )
    val taxCalc = StdBracketBasedTaxCalcFixture(brackets)

    val yearlyDetail = yearlyDetailFixture(2020)

    val noInflationRec = InflationRec(
        InflationRAC(0.0),
        InflationRAC(0.0),
        InflationRAC(0.0),
    )

    val cmpdInflation = 2.0
    val doubledInflation = noInflationRec.copy(
        std = InflationRAC(0.1, cmpdInflation, 2.2))

    should("determineTax calculates taxes based on brackets") {
        val currYear = yearlyDetail.copy(
            inflation = noInflationRec,
            filingStatus = FilingStatus.JOINTLY
        )

        // Less than first bracket, so amount should be 0
        taxCalc.determineTax(brackets[0].jointly.start / 2.0, currYear)
            .shouldBe(0.0)

        // Mid-way in 10% bracket
        taxCalc.determineTax(
            brackets[0].jointly.start + 0.5 * brackets[0].jointly.size(), currYear)
            .shouldBe(brackets[0].jointly.size() * 0.5 * brackets[0].pct)

        // Mid-way in 20% bracket
        taxCalc.determineTax(
            brackets[1].jointly.start + 0.5 * brackets[1].jointly.size(), currYear)
            .shouldBe(
                brackets[0].jointly.size() * brackets[0].pct +
                    brackets[1].jointly.size() * 0.5 * brackets[1].pct)

        // past final (40%) tax bracket
        taxCalc.determineTax(brackets[2].jointly.start + 10000.0, currYear)
            .shouldBe(
                brackets[0].jointly.size() * brackets[0].pct +
                    brackets[1].jointly.size() * brackets[1].pct +
                    10000.0 * brackets[2].pct)
    }

    should("determineTax adjusts brackets based on inflation") {
        val currYear = yearlyDetail.copy(
            inflation = doubledInflation,
            filingStatus = FilingStatus.JOINTLY
        )

        // Amount is greater than start of first bracket, but not when adjusted for inflation
        taxCalc.determineTax(brackets[0].jointly.start + 100.0, currYear)
            .shouldBe(0.0)

        // Amount is end of first bracket when adjusting for inflation
        taxCalc.determineTax(brackets[0].jointly.end * cmpdInflation, currYear)
            .shouldBe(brackets[0].jointly.size() * brackets[0].pct * cmpdInflation)

        // Amount is end of second bracket when adjusting for inflation
        taxCalc.determineTax(brackets[1].jointly.end * cmpdInflation, currYear)
            .shouldBe(
                (brackets[0].jointly.size() * brackets[0].pct +
                    brackets[1].jointly.size() * brackets[1].pct) * cmpdInflation)
    }

    should("marginalRate return the rate the matches tax bracket the inflation adjust amount") {
        val currYear = yearlyDetail.copy(
            inflation = doubledInflation,
            filingStatus = FilingStatus.JOINTLY
        )

        // Inflation adjusted amount is less than the start of the lowest brackets
        taxCalc.marginalRate(brackets[0].jointly.start * cmpdInflation - 1.0, currYear)
            .shouldBeZero()

        // Inflation adjusted amount is greater than the start of the lowest brackets but less than end
        taxCalc.marginalRate(brackets[0].jointly.start * cmpdInflation + 1.0, currYear)
            .shouldBe(brackets[0].pct)

        // Amount is so high it must be in the highest bracket
        taxCalc.marginalRate(1000000.0, currYear)
            .shouldBe(brackets[2].pct)
    }

    should("taxes are 0 if taxable income is <= 0") {
        val currYear = yearlyDetail.copy(
            inflation = doubledInflation,
            filingStatus = FilingStatus.JOINTLY
        )

        // Amount is greater than start of first bracket, but not when adjusted for inflation
        taxCalc.determineTax(-100000.0, currYear)
            .shouldBe(0.0)
    }

    should("determine top of current bracket") {
        val currYear = yearlyDetail.copy(
            inflation = doubledInflation,
            filingStatus = FilingStatus.JOINTLY
        )

        // Inflation adjusted top of first bracket
        taxCalc.currentBracket(brackets[0].jointly.start * cmpdInflation + 1.0, currYear)
            .shouldBe(brackets[0].jointly.applyInflation(cmpdInflation))

        // Inflation adjusted top of second bracket
        taxCalc.currentBracket(brackets[1].jointly.start * cmpdInflation + 1.0, currYear)
            .shouldBe(brackets[1].jointly.applyInflation(cmpdInflation))
    }

    should("determine greatest amount below given pct") {
        val currYear = yearlyDetail.copy(
            inflation = doubledInflation,
            filingStatus = FilingStatus.JOINTLY
        )

        // Inflation adjusted top of first bracket
        taxCalc.bracketBelowPct(brackets[0].pct + .01, currYear)
            .shouldBe(brackets[0].jointly.applyInflation(cmpdInflation))

        // Inflation adjusted top of second bracket
        taxCalc.bracketBelowPct(brackets[1].pct + .01, currYear)
            .shouldBe(brackets[1].jointly.applyInflation(cmpdInflation))
    }

    should("successfully load brackets from files") {
        fun validateBracketCases(cases: List<BracketCase>) {
            cases.fold(0.0) { acc, case ->
                case.start.shouldBeGreaterThanOrEqual(acc)
                case.end
            }
        }

        fun validateBrackets(brackets: List<TaxBracket>) {
            validateBracketCases(brackets.map { it.jointly })
            validateBracketCases(brackets.map { it.household })
            validateBracketCases(brackets.map { it.single })

            brackets.forEach {
                it.jointly.start.shouldBeGreaterThanOrEqual(it.household.start)
                it.household.start.shouldBeGreaterThanOrEqual(it.single.start)
                it.jointly.end.shouldBeGreaterThanOrEqual(it.household.end)
                it.household.end.shouldBeGreaterThanOrEqual(it.single.end)
            }
            brackets[brackets.size - 1].single.end.shouldBe(Double.MAX_VALUE)
            brackets[brackets.size - 1].jointly.end.shouldBe(Double.MAX_VALUE)
            brackets[brackets.size - 1].household.end.shouldBe(Double.MAX_VALUE)
        }

        validateBrackets(CurrentFedTaxBrackets.brackets)
        validateBrackets(RollbackFedTaxBrackets.brackets)
        validateBrackets(CurrentStateTaxBrackets.brackets)
        validateBrackets(FutureStateTaxBrackets.brackets)
        validateBrackets(CurrentFedLTGBrackets.brackets)
        validateBrackets(RollbackFedLTGBrackets.brackets)
    }
})

class StdBracketBasedTaxCalcFixture(override val brackets: List<TaxBracket>)
    : StdBracketBasedTaxCalc, CmpdInflationProvider by StdCmpdInflationProvider()
