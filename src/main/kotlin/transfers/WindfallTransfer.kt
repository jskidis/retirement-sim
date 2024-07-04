package transfers

import Amount
import RecIdentifier
import Year
import YearlyDetail
import asset.AssetChange
import config.SimConfig
import tax.NonTaxableProfile
import tax.TaxabilityProfile
import util.RecFinder

class WindfallTransfer(
    val amount: Amount,
    val year: Year,
    val destAcctIdent: RecIdentifier,
    val transferName: String = "Windfall",
    override val taxabilityProfile: TaxabilityProfile = NonTaxableProfile(),
) : DestOnlyTransfer() {
    override val sourceDestPairs = listOf(dummySourceRec to destAcctIdent)
    override fun transferName(): String = transferName

    override fun determineTransferAmount(config: SimConfig, currYear: YearlyDetail): Amount =
        if (amount == 0.0 || currYear.year != year) 0.0
        else amount

    override fun generateTransfers(currYear: YearlyDetail, amount: Amount): List<TransferRec> {
        val destRec = RecFinder.findAssetRec(destAcctIdent, currYear)
        return if (destRec == null) listOf() // should probably generate an error
        else listOf(buildTransferRec(destRec, getDestTribution(amount)))
    }

    private fun getDestTribution(amount: Amount) = AssetChange(
        name = transferName,
        amount = amount,
        taxabilityProfile.calcTaxable(destAcctIdent.person, amount)
    )
}