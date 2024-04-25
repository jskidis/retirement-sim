package cashflow

import Amount
import YearlyDetail
import asset.AssetChange
import asset.AssetRec
import config.EmploymentConfig
import config.Person
import tax.TaxabilityProfile
import util.RecFinder

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
            val incomeRec = RecFinder.findIncomeRec(empConfig.ident, currYear)
            if (incomeRec == null) null
            else {
                val amount = (if (amountRetriever.doProrate()) pctInYear else 1.0) *
                    amountRetriever.determineAmount(currYear, incomeRec, person.birthYM)
                createCashflowEvent(amount)
            }
        }
    }

    private fun createCashflowEvent(amount: Amount): AssetChange {
        return AssetChange(
            name = contributionName,
            amount = amount,
            taxable = taxabilityProfile?.calcTaxable(empConfig.ident.person, amount),
            cashflow = if (amountRetriever.isFreeMoney()) 0.0 else -amount
        )
    }
}

