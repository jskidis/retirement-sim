package asset

import Amount
import Rate
import YearlyDetail
import tax.TaxCalcConfig
import tax.TaxableAmounts


interface RothConversionAmountCalc {
    fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount
}

class TilNextBracketRothConv : RothConversionAmountCalc {
    override fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount {
        val taxable = taxableAmounts.fed
        val topOfBracket = taxCalcConfig.fed.topOfCurrBracket(taxable, currYear)
        return if (topOfBracket == Amount.MAX_VALUE) 0.0
        else topOfBracket - taxable
    }
}

class MaxTaxRateRothConv(val maxPct: Rate) : RothConversionAmountCalc {
    override fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount {
        val taxable = taxableAmounts.fed
        val topAmount = taxCalcConfig.fed.topAmountBelowPct(maxPct, currYear)
        return topAmount - taxable
    }
}