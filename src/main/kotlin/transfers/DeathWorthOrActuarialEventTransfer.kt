package transfers

import Amount
import Name
import RecIdentifier
import YearlyDetail
import asset.AssetChange
import config.SimConfig
import departed.ActuarialEvent
import tax.NonTaxableProfile
import tax.TaxabilityProfile
import util.RecFinder

class DeathWorthOrActuarialEventTransfer(
    val person: Name,
    val assetThreshold: Amount,
    val actuarialEventCalc: ActuarialEvent,
    val transferName: Name,
    val assetIdent: RecIdentifier,
    val depositAcctIdent: RecIdentifier,
    override val taxabilityProfile: TaxabilityProfile = NonTaxableProfile(),
) : TransferGenerator {

    override val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>> =
        listOf(assetIdent to depositAcctIdent)

    override fun transferName(): String = transferName

    override fun determineTransferAmount(config: SimConfig, currYear: YearlyDetail): Amount {
        val assetRec = RecFinder.findAssetRec(assetIdent, currYear)
        return if (assetRec != null && shouldSell(config, currYear)) assetRec.finalBalance()
        else 0.0
    }

    override fun generateTransfers(currYear: YearlyDetail, amount: Amount): List<TransferRec> {
        val assetRec = RecFinder.findAssetRec(assetIdent, currYear)
        val depositRec = RecFinder.findAssetRec(depositAcctIdent, currYear)

        return if (assetRec == null || depositRec == null) return listOf()
        else {
            listOf(TransferRec(
                sourceRec = assetRec,
                sourceTribution = AssetChange(transferName, -amount),
                destRec = depositRec,
                destTribution = AssetChange(transferName, amount)
            ))
        }
    }

    private fun shouldSell(config: SimConfig, currYear: YearlyDetail): Boolean {
        val personDeparted =
            (config.nonDepartedMembers(currYear).find {
                it.name().equals(person)
            }) == null

        val actuarialEventOccurred = actuarialEventCalc.didEventOccur(currYear)

        val assetsBelowThreshold = assetThreshold >
            currYear.totalAssetValues() / currYear.inflation.std.cmpdEnd

        return personDeparted || actuarialEventOccurred || assetsBelowThreshold
    }
}

