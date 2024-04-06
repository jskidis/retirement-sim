package tax

import YearlyDetail
import config.SimConfig
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.*

interface ITaxesProcessor {
    fun processTaxes(currYear: YearlyDetail, config: SimConfig): TaxesRec
    fun determineTaxableAmounts(currYear: YearlyDetail): TaxableAmounts
    fun determineStdDeduct(currYear: YearlyDetail): Double
}

object TaxesProcessor : ITaxesProcessor, CmpdInflationProvider by StdCmpdInflationProvider()  {
    val nameOfTaxablePerson = "Household"

    override fun processTaxes(
        currYear: YearlyDetail,
        config: SimConfig,
    ): TaxesRec {
        val taxable = determineTaxableAmounts(currYear)

        val ltgTaxes = config.taxConfig.fedLTG.marginalRate(
            taxable.fed + taxable.fedLTG, currYear) * taxable.fedLTG

        return TaxesRec(
            fed = config.taxConfig.fed.determineTax(taxable.fed, currYear) + ltgTaxes,
            state = config.taxConfig.state.determineTax(taxable.state, currYear),
            socSec = config.taxConfig.socSec.determineTax(taxable.socSec, currYear),
            medicare = config.taxConfig.medicare.determineTax(taxable.medicare, currYear),
            agi = taxable.fed + taxable.fedLTG
        )
    }

    override fun determineTaxableAmounts(currYear: YearlyDetail)
        : TaxableAmounts {
        val taxableAmounts =
            currYear.incomes.map { it.taxableIncome } +
                currYear.expenses.map { it.taxDeductions } +
                currYear.assets.map { it.taxable() } +
                currYear.benefits.map { it.taxableAmount }

        val stdDeduct = determineStdDeduct(currYear)

        return taxableAmounts.filter { it.hasAmounts() }
            .fold(
                TaxableAmounts(person = nameOfTaxablePerson),
                { acc, amounts -> acc.plus(amounts) }
            ).plus(TaxableAmounts(
                person = nameOfTaxablePerson,
                fed = -stdDeduct,
                state = -stdDeduct
            ))
    }

    override fun determineStdDeduct(currYear: YearlyDetail): Double =
        when (currYear.filingStatus) {
            FilingStatus.JOINTLY -> ConstantsProvider.getValue(STD_DEDUCT_JOINTLY)
            FilingStatus.SINGLE -> ConstantsProvider.getValue(STD_DEDUCT_SINGLE)
            FilingStatus.HOUSEHOLD -> ConstantsProvider.getValue(STD_DEDUCT_HOUSEHOLD)
        } * currYear.inflation.std.cmpdStart
}
