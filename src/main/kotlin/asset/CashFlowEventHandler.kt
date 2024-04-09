package asset

import Amount
import Year
import config.Person
import tax.TaxableAmounts

interface CashFlowEventHandler {
    fun generateCashFlowTribution(balance: Amount, year: Year): AssetChange?

}

open class RmdCashFlowEventHandler(val person: Person) : CashFlowEventHandler, RmdPctLookup {

    companion object { const val CHANGE_NAME = "RMD" }

    override fun generateCashFlowTribution(balance: Amount, year: Year): AssetChange? {
        val pct = getRmdPct(age = year - person.birthYM.year)
        return if (pct == 0.0) null
        else createAssetChange(amount = pct * balance, person)
    }

    private fun createAssetChange(amount: Amount, person: Person): AssetChange =
        AssetChange(
            name = CHANGE_NAME,
            amount = -amount,
            taxable = TaxableAmounts(person = person.name, fed = amount, state = amount),
            isCashflowEvent = true
        )

    override fun getRmdPct(age: Int): Double = RmdPct.getRmdPct(age)
}
