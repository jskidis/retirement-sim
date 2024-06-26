package tax

import Amount
import Rate
import YearlyDetail
import config.SimConfig
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import util.SingleYearBasedConfig

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
    val currBracket: BracketCase? = null,
    val backetBelowPct: BracketCase? = null,
) : BracketBasedTaxCalc {
    override fun determineTax(taxableAmount: Amount, currYear: YearlyDetail): Amount = tax
    override fun marginalRate(taxableAmount: Amount, currYear: YearlyDetail): Rate = marginalRate
    override fun currentBracket(taxableAmount: Amount, currYear: YearlyDetail): BracketCase? =
        currBracket

    override fun bracketBelowPct(pct: Rate, currYear: YearlyDetail): BracketCase? = backetBelowPct
}

fun baseTaxConfigFixture(
    fed: BracketBasedTaxCalc = BracketTaxCalcFixture(),
) = TaxCalcConfig(
    fed = fed,
    fedLTG = BracketTaxCalcFixture(),
    state = TaxCalcFixture(),
    socSec = TaxCalcFixture(),
    medicare = TaxCalcFixture(),
)

fun taxConfigFixture(taxCalc: TaxCalcConfig = baseTaxConfigFixture()) =
    SingleYearBasedConfig(taxCalc)

class TaxesProcessorFixture(
    val taxesRec: TaxesRec = TaxesRec(),
    val taxableAmounts: TaxableAmounts = TaxableAmounts("Person"),
    val stdDeduction: Amount = 0.0,
    val filingStatus: FilingStatus = FilingStatus.JOINTLY,
) : TaxProcessorConfig {
    override fun processTaxes(currYear: YearlyDetail, config: SimConfig) = taxesRec
    override fun determineTaxableAmounts(currYear: YearlyDetail) = taxableAmounts
    override fun determineStdDeduct(currYear: YearlyDetail) = stdDeduction
    override fun determineFilingStatus(prevYear: YearlyDetail?, config: SimConfig) = filingStatus
}

