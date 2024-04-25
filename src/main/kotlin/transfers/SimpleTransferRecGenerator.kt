package transfers

import Amount
import Name
import asset.AssetChange
import asset.AssetRec
import tax.TaxabilityProfile

open class SimpleTransferRecGenerator(
    val transferName: Name,
    val taxabilityProfile: TaxabilityProfile,
) : TransferRecGenerator {

    override fun transferName() = transferName
    override fun generateTransfer(distribution: Amount, sourceRec: AssetRec, destRec: AssetRec) =
        TransferRec(
            sourceRec = sourceRec,
            sourceTribution = AssetChange(
                name = transferName(), amount = -distribution
            ),
            destRec = destRec,
            destTribution = AssetChange(
                name = transferName(), amount = distribution,
                taxable = taxabilityProfile.calcTaxable(sourceRec.ident.person, distribution),
                isCarryOver = true
            ))
}