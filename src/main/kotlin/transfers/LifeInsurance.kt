package transfers

import Amount
import Name
import RecIdentifier
import Year
import YearlyDetail
import asset.AssetChange
import asset.AssetRec
import asset.AssetType
import config.SimConfig
import tax.NonTaxableProfile
import tax.TaxabilityProfile
import util.RecFinder

class LifeInsurance(
    val policyName: Name,
    val coveredPerson: Name,
    val coverageAmount: Amount,
    val yearRange: IntRange,
    val destAcctIdent: RecIdentifier,
    override val taxabilityProfile: TaxabilityProfile = NonTaxableProfile(),
) : TransferGenerator {
    override val sourceDestPairs = listOf(dummySourceRec to destAcctIdent)

    companion object {
        val dummySourceRec = RecIdentifier("InsuranceCo", "Nobody")
    }

    override fun transferName(): String = policyName

    override fun determineTransferAmount(config: SimConfig, currYear: YearlyDetail): Amount =
        if (currYear.departed.find { it.person == coveredPerson && it.year == currYear.year } == null ||
            !yearRange.contains(currYear.year)) 0.0
        else coverageAmount

    override fun generateTransfers(currYear: YearlyDetail, amount: Amount): List<TransferRec> {
        val destRec = RecFinder.findAssetRec(destAcctIdent, currYear)
        return if (destRec == null) listOf() // should probably generate an error
        else listOf(
            TransferRec(
                sourceRec = buildDummySourceRec(currYear.year),
                sourceTribution = buildDummyTribution(),
                destRec = destRec,
                destTribution = getDestTribution(amount)
            )
        )
    }

    private fun getDestTribution(amount: Amount) = AssetChange(
        name = policyName,
        amount = amount,
        taxabilityProfile.calcTaxable(coveredPerson, amount)
    )

    private fun buildDummySourceRec(year: Year) = AssetRec(
        year = year,
        ident = dummySourceRec,
        assetType = AssetType.CASH,
        startBal = coverageAmount,
        startUnrealized = 0.0,
        gains = AssetChange("NoGain", 0.0)
    )

    private fun buildDummyTribution() = AssetChange("Insurance Payout", 0.0)
}