package tax

import config.configFixture
import inflation.InflationRAC
import inflation.InflationRec
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class BracketBasedTaxCalcTest: ShouldSpec({
    val brackets = listOf(
        TaxBracket(.1, 1000.0, 2000.0),
        TaxBracket(.2, 2000.0, 4000.0),
        TaxBracket(.4, 4000.0),
    )
    val taxCalc = BracketBasedTaxCalcFixture(brackets)

    val config = configFixture()
    val yearlyDetail = yearlyDetailFixture(2020)

    val noInflationRec = InflationRec(
        InflationRAC(0.0),
        InflationRAC(0.0),
        InflationRAC(0.0),
        InflationRAC(0.0),
    )

    val doubledInflation = noInflationRec.copy(
        chain = InflationRAC(0.1, 2.0, 2.2))

    should("determineTax should calculate texas based on brackets") {
        val currYear = yearlyDetail.copy(inflation = noInflationRec)

        // Less than first bracket, so amount should be 0
        taxCalc.determineTax(brackets[0].start / 2.0, config, currYear)
            .shouldBe(0.0)

        // Mid-way in 10% bracket
        taxCalc.determineTax(brackets[0].start + 0.5 * brackets[0].size(), config, currYear)
            .shouldBe(brackets[0].size() * 0.5 * brackets[0].pct)

        // Mid-way in 20% bracket
        taxCalc.determineTax(brackets[1].start + 0.5 * brackets[1].size(), config, currYear)
            .shouldBe(brackets[0].size() * brackets[0].pct +
                brackets[1].size() * 0.5 * brackets[1].pct)

        // past final (40%) tax bracket
        taxCalc.determineTax(brackets[2].start + 10000.0, config, currYear)
            .shouldBe(brackets[0].size() * brackets[0].pct +
                brackets[1].size() * brackets[1].pct +
                10000.0 * brackets[2].pct)
    }

    should("determineTax should adjust brackets based on inflation") {
        val currYear = yearlyDetail.copy(inflation = doubledInflation)

        // Amount is greater than start of first bracket, but not when adjusted for inflation
        taxCalc.determineTax(brackets[0].start + 100.0, config, currYear)
            .shouldBe(0.0)

        // Amount is end of first bracket when adjusting for inflation
        taxCalc.determineTax(brackets[0].end * 2.0, config, currYear)
            .shouldBe(brackets[0].size() * brackets[0].pct * 2.0)

        // Amount is end of second bracket when adjusting for inflation
        taxCalc.determineTax(brackets[1].end * 2.0, config, currYear)
            .shouldBe((brackets[0].size() * brackets[0].pct +
                brackets[1].size() * brackets[1].pct) * 2.0)
    }
})

class BracketBasedTaxCalcFixture(override val brackets: List<TaxBracket>) : BracketBasedTaxCalc
