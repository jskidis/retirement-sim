package tax

import YearlyDetail
import config.MainConfig

object TaxesProcessor {
    val nameOfTaxablePerson = "Household"

    fun processTaxes(currYear: YearlyDetail, config: MainConfig): TaxesRec {
        val taxable = determineTaxableAmounts(currYear)
        return TaxesRec(
            fed = config.taxConfig.fed.determineTax(taxable.fed, currYear),
            state = config.taxConfig.state.determineTax(taxable.state, currYear),
            socSec = config.taxConfig.socSec.determineTax(taxable.socSec, currYear),
            medicare = config.taxConfig.medicare.determineTax(taxable.medicare, currYear),
        )
    }

    private fun determineTaxableAmounts(currYear: YearlyDetail): TaxableAmounts {
        val taxableAmounts =
            currYear.incomes.map { it.taxableIncome } +
            currYear.expenses.map { it.taxDeductions }

        return taxableAmounts.filter { it.hasAmounts() }
            .fold(TaxableAmounts(person = nameOfTaxablePerson), { acc, amounts -> acc.plus(amounts) })
    }
}