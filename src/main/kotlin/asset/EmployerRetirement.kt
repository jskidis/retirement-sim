package asset

import Amount
import YearlyDetail
import config.EmploymentConfig
import config.Person
import income.IncomeRec
import tax.TaxabilityProfile

class EmployerRetirement(
    val empConfig: EmploymentConfig,
    val person: Person,
    val contributionName: String,
    val amountRetriever: EmpRetirementAmountRetriever,
    val taxabilityProfile: TaxabilityProfile? = null,
) : CashFlowEventHandler {

    override fun generateCashFlowTribution(assetRec: AssetRec, currYear: YearlyDetail)
        : AssetChange? {
        val pctInYear = empConfig.dateRange.pctInYear(currYear.year).value

        return if (pctInYear == 0.0) null
        else {
            val incomeRec = findIncomeRec(currYear)
            if (incomeRec == null) null
            else {
                val amount = (if (amountRetriever.doProrate()) pctInYear else 1.0) *
                    amountRetriever.determineAmount(currYear, incomeRec, person.birthYM)
                createCashflowEvent(amount)
            }
        }
    }

    private fun findIncomeRec(currYear: YearlyDetail): IncomeRec? =
        currYear.incomes.find { it.ident == empConfig.ident }

    private fun createCashflowEvent(amount: Amount): AssetChange {
        return AssetChange(
            name = contributionName,
            amount = amount,
            taxable = taxabilityProfile?.calcTaxable(empConfig.ident.person, amount),
            cashflow = if (amountRetriever.isFreeMoney()) 0.0 else -amount
        )
    }
}

