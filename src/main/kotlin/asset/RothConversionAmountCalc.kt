package asset

import Amount
import YearlyDetail
import tax.TaxCalcConfig
import tax.TaxableAmounts


fun interface RothConversionAmountCalc {
    fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount
}

