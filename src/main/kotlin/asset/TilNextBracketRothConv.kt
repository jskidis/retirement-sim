package asset

import Amount
import YearlyDetail
import tax.TaxCalcConfig
import tax.TaxableAmounts

class TilNextBracketRothConv() : RothConversionAmountCalc {
    override fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount {
        val taxable = taxableAmounts.fed + taxableAmounts.fedLTG
        val currBracket = taxCalcConfig.fed.currentBracket(taxable, currYear)
        return if (currBracket == null || currBracket.end == Amount.MAX_VALUE) 0.0
        else currBracket.end - taxable
    }
}