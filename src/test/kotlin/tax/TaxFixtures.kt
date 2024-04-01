package tax

import Amount
import YearlyDetail

class TaxCalcFixture(val fixedPct: Double = 0.0) : TaxCalculator {
    override fun determineTax(taxableAmount: Amount, currYear: YearlyDetail): Amount = fixedPct
}

class BracketTaxCalcFixture(fixedPct: Double = 0.0) : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> = listOf(
        TaxBracket(
            pct = fixedPct,
            single = BracketCase(fixedPct),
            jointly = BracketCase(fixedPct),
            household = BracketCase(fixedPct)
        )
    )
}

fun taxConfigFixture() = TaxCalcConfig(
    fed = BracketTaxCalcFixture(),
    fedLTG = BracketTaxCalcFixture(),
    state = TaxCalcFixture(),
    socSec = TaxCalcFixture(),
    medicare = TaxCalcFixture(),
)

