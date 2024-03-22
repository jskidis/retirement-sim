package asset

import Amount
import Year
import config.Person
import tax.TaxableAmounts

interface RequiredDistHandler {
    fun generateDistribution(balance: Amount, year: Year): AssetChange?

    companion object { const val CHANGE_NAME = "ReqDist" }
}

class NullRequestDist : RequiredDistHandler {
    override fun generateDistribution(balance: Amount, year: Year): AssetChange? = null
}

open class RmdRequiredDistHandler(val person: Person) : RequiredDistHandler, RmdPctLookup {

    override fun generateDistribution(balance: Amount, year: Year): AssetChange? {
        val pct = getRmdPct(age = year - person.birthYM.year)
        return if (pct == 0.0) null
        else createAssetChange(amount = pct * balance, person)
    }

    private fun createAssetChange(amount: Amount, person: Person): AssetChange =
        AssetChange(
            name = RequiredDistHandler.CHANGE_NAME,
            amount = -amount,
            taxable = TaxableAmounts(person = person.name, fed = amount, state = amount),
            isReqDist = true
        )

    override fun getRmdPct(age: Int): Double = RmdPct.getRmdPct(age)
}
