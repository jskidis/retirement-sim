package asset

import Amount
import YearlyDetail
import config.Person
import tax.TaxabilityProfile

open class RmdCashFlowEventHandler(
    val person: Person,
    val taxabilityProfile: TaxabilityProfile
) : CashFlowEventHandler, RmdPctLookup {

    companion object { const val CHANGE_NAME = "RMD" }

    override fun generateCashFlowTribution(assetRec: AssetRec, currYear: YearlyDetail): AssetChange? {
        val pct = getRmdPct(age = currYear.year - person.birthYM.year)
        return if (pct == 0.0) null
        else {
            createAssetChange(amount = pct * assetRec.startBal, person)
        }

    }

    private fun createAssetChange(amount: Amount, person: Person): AssetChange =
        AssetChange(
            name = CHANGE_NAME,
            amount = -amount,
            taxable = taxabilityProfile.calcTaxable(person = person.name, amount),
            cashflow = amount
        )

    override fun getRmdPct(age: Int): Double = RmdPct.getRmdPct(age)
}
