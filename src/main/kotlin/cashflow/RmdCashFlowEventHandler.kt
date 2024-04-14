package cashflow

import Amount
import YearlyDetail
import asset.AssetChange
import asset.AssetRec
import asset.RothConversionProcessor
import config.Person
import tax.NonWageTaxableProfile
import tax.TaxabilityProfile

open class RmdCashFlowEventHandler(
    val person: Person,
    val taxabilityProfile: TaxabilityProfile = NonWageTaxableProfile(),
    val rmdPctLookup: RmdPctLookup = RmdPct,
) : CashFlowEventHandler {

    companion object {
        const val CHANGE_NAME = "RMD"
    }

    override fun generateCashFlowTribution(
        assetRec: AssetRec,
        currYear: YearlyDetail,
    ): AssetChange? {
        val age = currYear.year - person.birthYM.year
        val pct = rmdPctLookup.getRmdPct(age = currYear.year - person.birthYM.year)

        return if (age < RothConversionProcessor.rmdMinAge(currYear.year) || pct == 0.0) null
        else createAssetChange(amount = pct * assetRec.startBal, person)

    }

    private fun createAssetChange(amount: Amount, person: Person): AssetChange =
        AssetChange(
            name = CHANGE_NAME,
            amount = -amount,
            taxable = taxabilityProfile.calcTaxable(person = person.name, amount),
            cashflow = amount
        )
}
