package transfers

import Amount
import Rate
import YearlyDetail
import tax.TaxCalcConfig
import tax.TaxableAmounts

class MaxTaxRateRothConv(
    val maxPct: Rate,
    val pctOfHighestBracket: Rate = 1.00,
) : RothConversionAmountCalc {
    override fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount {
        val taxable = taxableAmounts.fed + taxableAmounts.fedLTG
        val bracket = taxCalcConfig.fed.bracketBelowPct(maxPct, currYear)
        return if (bracket == null || bracket.end == Amount.MAX_VALUE) 0.0
        else Math.max(0.0,
            (bracket.end - bracket.start) * pctOfHighestBracket + bracket.start - taxable
        )
    }
}