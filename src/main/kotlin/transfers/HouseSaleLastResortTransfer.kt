package transfers

import Amount
import RecIdentifier
import YearlyDetail
import asset.AssetChange
import config.SimConfig
import tax.NonTaxableProfile
import tax.TaxabilityProfile
import util.RecFinder

class HouseSaleLastResortTransfer(
    val houseIdent: RecIdentifier,
    val depositAcctIdent: RecIdentifier,
    override val taxabilityProfile: TaxabilityProfile = NonTaxableProfile(),
) : TransferGenerator {

    override val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>> =
        listOf(houseIdent to depositAcctIdent)

    override fun transferName(): String = TRANSFER_NAME

    companion object {
        const val TRANSFER_NAME = "House-Sale"
    }

    override fun determineTransferAmount(config: SimConfig, currYear: YearlyDetail): Amount {
        val balance = RecFinder.findAssetRec(houseIdent, currYear)?.finalBalance() ?: 0.0
        val netExpenses = currYear.totalExpense() - currYear.totalBenefits()

        return if (balance == 0.0 || currYear.totalAssetValues() - balance > netExpenses) 0.0
        else balance
    }

    override fun generateTransfers(currYear: YearlyDetail, amount: Amount): List<TransferRec> {
        val assetAcct = RecFinder.findAssetRec(houseIdent, currYear)
        val destAcct = RecFinder.findAssetRec(depositAcctIdent, currYear)

        return if (assetAcct == null || destAcct == null) listOf()
        else listOf(
            TransferRec(
                sourceRec = assetAcct,
                sourceTribution = AssetChange(TRANSFER_NAME, -amount),
                destRec = destAcct,
                destTribution = AssetChange(TRANSFER_NAME, amount)
            ))
    }
}