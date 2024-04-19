package transfers

import Amount
import YearMonth
import YearlyDetail
import inflation.INFL_TYPE
import tax.TaxCalcConfig
import tax.TaxableAmounts
import util.RetirementLimits

class RothIncomeLimitRothConv(
    val primaryAmountCalc: RothConversionAmountCalc,
    val inflationType: INFL_TYPE = INFL_TYPE.STD
) : RothConversionAmountCalc {
    override fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount {
        val amountToRothLimit = amountToRothIncomeLimit(currYear,
            taxableAmount = taxableAmounts.fed + taxableAmounts.fedLTG)

        val primaryAmount = primaryAmountCalc.amountToConvert(
            currYear, taxableAmounts, taxCalcConfig)

        return Math.max(0.0, Math.min(amountToRothLimit, primaryAmount))
    }

    private fun amountToRothIncomeLimit(currYear: YearlyDetail, taxableAmount: Amount)
    : Amount {
        val rothIncomeLimit = RetirementLimits.rothIncomeLimit(currYear)

        val maxIRAContribution =
            RetirementLimits.calcIRACap(currYear = currYear, inflType = inflationType)

        val maxCatchupContribution =
            RetirementLimits.calcIRACatchup(
                currYear = currYear,
                birthYM = YearMonth(1900), // assume person is old enough to make catch contribution as well
                inflType = inflationType)

        return rothIncomeLimit - maxIRAContribution - maxCatchupContribution - taxableAmount
    }
}