package tax

import YearlyDetail
import config.SimConfig

object TaxesProcessor {
    val nameOfTaxablePerson = "Household"

    fun processTaxes(
        currYear: YearlyDetail,
        carryOverTaxable: List<TaxableAmounts>,
        config: SimConfig,
    ): TaxesRec {
        val taxable = determineTaxableAmounts(currYear, carryOverTaxable)
        val ltgTaxes = config.taxConfig.fedLTG.marginalRate(
            taxable.fed + taxable.fedLTG, currYear) * taxable.fedLTG

        return TaxesRec(
            fed = config.taxConfig.fed.determineTax(taxable.fed, currYear) + ltgTaxes,
            state = config.taxConfig.state.determineTax(taxable.state, currYear),
            socSec = config.taxConfig.socSec.determineTax(taxable.socSec, currYear),
            medicare = config.taxConfig.medicare.determineTax(taxable.medicare, currYear),
        )
    }

    fun determineTaxableAmounts(currYear: YearlyDetail, carryOverTaxable: List<TaxableAmounts>)
        : TaxableAmounts {
        val taxableAmounts =
            currYear.incomes.map { it.taxableIncome } +
                currYear.expenses.map { it.taxDeductions } +
                currYear.assets.map { it.taxable() } +
                currYear.benefits.map { it.taxableAmount } +
                carryOverTaxable

        return taxableAmounts.filter { it.hasAmounts() }
            .fold(
                TaxableAmounts(person = nameOfTaxablePerson),
                { acc, amounts -> acc.plus(amounts) })
    }

    fun carryOverTaxable(currYear: YearlyDetail): List<TaxableAmounts> {
        return currYear.assets.flatMap { asset ->
            asset.tributions.filter { tribution ->
                tribution.isCarryOver
            }.map { tribution ->
                tribution.taxable
            }
        }.mapNotNull { it }
    }
}
