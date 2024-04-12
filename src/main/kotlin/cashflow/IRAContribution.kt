package cashflow

import Amount
import Name
import Rate
import YearlyDetail
import asset.AssetChange
import asset.AssetRec
import config.Person
import tax.TaxabilityProfile
import util.RetirementLimits

class IRAContribution(
    val person: Person,
    val contribName: Name,
    val pctOfCap: Rate,
    val taxabilityProfile: TaxabilityProfile? = null,
    val includeCatchup: Boolean = false,
) : CashFlowEventHandler {

    override fun generateCashFlowTribution(assetRec: AssetRec, currYear: YearlyDetail)
        : AssetChange? {

        val personIncome = personIncome(person.name, currYear)
        val maxAmount = RetirementLimits.calcIRACap(currYear) +
            if (!includeCatchup) 0.0
            else RetirementLimits.calcIRACatchup(currYear, person.birthYM)
        val amount = Math.min(personIncome, maxAmount * pctOfCap)

        return if (amount == 0.0) null
        else createCashflowEvent(amount)
    }

    private fun createCashflowEvent(amount: Amount): AssetChange =
        AssetChange(
            name = contribName,
            amount = amount,
            taxable = taxabilityProfile?.calcTaxable(person.name, amount),
            cashflow = -amount
        )

    private fun personIncome(person: Name, currYear: YearlyDetail): Amount =
        currYear.incomes.filter { it.ident.person == person }
            .sumOf { it.taxable().socSec }
}