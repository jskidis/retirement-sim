package transfers

import Amount
import YearlyDetail
import tax.TaxCalcConfig
import tax.TaxableAmounts

class RothConversionAmountCalcFixture(val amountToConvert: Amount) : RothConversionAmountCalc {
    override fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount = amountToConvert
}
