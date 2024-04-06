package tax

import Amount
import Rate
import YearlyDetail
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider

class TaxCalcFixture(val fixedPct: Double = 0.0) : TaxCalculator {
    override fun determineTax(taxableAmount: Amount, currYear: YearlyDetail): Amount = fixedPct
}

class BracketTaxCalcFixture(fixedPct: Double = 0.0) : StdBracketBasedTaxCalc,
    CmpdInflationProvider by StdCmpdInflationProvider() {
    override val brackets: List<TaxBracket> = listOf(
        TaxBracket(
            pct = fixedPct,
            single = BracketCase(fixedPct),
            jointly = BracketCase(fixedPct),
            household = BracketCase(fixedPct)
        )
    )
}

class BracketBasedTaxCalcFixture(
    val tax: Amount = 0.0,
    val marginalRate: Rate = 0.0,
    val topOfCurrBracket: Amount = 0.0,
    val topAmountBelowPct: Amount = 0.0,
) : BracketBasedTaxCalc {
    override fun determineTax(taxableAmount: Amount, currYear: YearlyDetail): Amount = tax
    override fun marginalRate(taxableAmount: Amount, currYear: YearlyDetail): Rate = marginalRate
    override fun topOfCurrBracket(taxableAmount: Amount, currYear: YearlyDetail): Amount = topOfCurrBracket
    override fun topAmountBelowPct(pct: Rate, currYear: YearlyDetail): Amount = topAmountBelowPct
}


fun taxConfigFixture() = TaxCalcConfig(
    fed = BracketTaxCalcFixture(),
    fedLTG = BracketTaxCalcFixture(),
    state = TaxCalcFixture(),
    socSec = TaxCalcFixture(),
    medicare = TaxCalcFixture(),
)

