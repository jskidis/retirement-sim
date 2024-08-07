package transfers

import Amount
import Name
import RecIdentifier
import YearlyDetail
import asset.AssetChange
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
) : DestOnlyTransfer() {

    override val sourceDestPairs = listOf(dummySourceRec to destAcctIdent)
    override fun transferName(): String = policyName

    override fun determineTransferAmount(config: SimConfig, currYear: YearlyDetail): Amount =
        if (currYear.departed.find { it.person == coveredPerson && it.year == currYear.year } == null ||
            !yearRange.contains(currYear.year)) 0.0
        else coverageAmount

    override fun generateTransfers(currYear: YearlyDetail, amount: Amount): List<TransferRec> {
        val destRec = RecFinder.findAssetRec(destAcctIdent, currYear)
        return if (destRec == null) listOf() // should probably generate an error
        else listOf(buildTransferRec(destRec, getDestTribution(coverageAmount)))
    }

    private fun getDestTribution(amount: Amount) = AssetChange(
        name = policyName,
        amount = amount,
        taxabilityProfile.calcTaxable(coveredPerson, amount)
    )
}