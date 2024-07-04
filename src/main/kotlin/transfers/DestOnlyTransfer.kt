package transfers

import RecIdentifier
import asset.AssetChange
import asset.AssetRec
import asset.AssetType

abstract class DestOnlyTransfer : TransferGenerator {
    companion object {
        val dummySourceRec = RecIdentifier("Dummy", "Nobody")
    }

    fun buildTransferRec(destRec: AssetRec, destTribution: AssetChange): TransferRec =
        TransferRec(
            sourceRec = buildDummySourceRec(),
            sourceTribution = buildDummyTribution(),
            destRec = destRec,
            destTribution = destTribution
        )

    private fun buildDummySourceRec() = AssetRec(
        year = 0,
        ident = dummySourceRec,
        assetType = AssetType.OTHER,
        startBal = 0.0,
        startUnrealized = 0.0,
        gains = AssetChange("NoGain", 0.0)
    )

    private fun buildDummyTribution() = AssetChange("Dummy", 0.0)

}