package tax

import YearlyDetail
import config.SimConfig
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.*

object TaxesProcessor {
    val nameOfTaxablePerson = "Household"

    fun processTaxes(
        currYear: YearlyDetail,
        config: SimConfig,
    ): TaxesRec {
        val taxable = determineTaxableAmounts(currYear)

        val stdDeduct = determineStdDeduct(currYear)

        val ltgTaxes = config.taxConfig.fedLTG.marginalRate(
            taxable.fed + taxable.fedLTG - stdDeduct, currYear) * taxable.fedLTG

        return TaxesRec(
            fed = config.taxConfig.fed.determineTax(taxable.fed - stdDeduct, currYear) + ltgTaxes,
            state = config.taxConfig.state.determineTax(taxable.state - stdDeduct, currYear),
            socSec = config.taxConfig.socSec.determineTax(taxable.socSec, currYear),
            medicare = config.taxConfig.medicare.determineTax(taxable.medicare, currYear),
            agi = taxable.fed + taxable.fedLTG - stdDeduct
        )
    }

    fun determineTaxableAmounts(currYear: YearlyDetail)
        : TaxableAmounts {
        val taxableAmounts =
            currYear.incomes.map { it.taxableIncome } +
                currYear.expenses.map { it.taxDeductions } +
                currYear.assets.map { it.taxable() } +
                currYear.benefits.map { it.taxableAmount }

        return taxableAmounts.filter { it.hasAmounts() }
            .fold(
                TaxableAmounts(person = nameOfTaxablePerson),
                { acc, amounts -> acc.plus(amounts) })
    }

    fun determineStdDeduct(currYear: YearlyDetail): Double =
        when (currYear.filingStatus) {
            FilingStatus.JOINTLY -> ConstantsProvider.getValue(STD_DEDUCT_JOINTLY)
            FilingStatus.SINGLE -> ConstantsProvider.getValue(STD_DEDUCT_SINGLE)
            FilingStatus.HOUSEHOLD -> ConstantsProvider.getValue(STD_DEDUCT_HOUSEHOLD)
        } * currYear.inflation.std.cmpdStart
}
