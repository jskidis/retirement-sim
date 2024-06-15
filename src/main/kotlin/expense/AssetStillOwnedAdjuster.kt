package expense

import Amount
import RecIdentifier
import YearlyDetail
import progression.AmountAdjusterWithGapFiller
import util.RecFinder

class AssetStillOwnedAdjuster(val assetIdent: RecIdentifier) : AmountAdjusterWithGapFiller {

    override fun adjustGapFillValue(value: Amount, prevYear: YearlyDetail): Amount =
        adjustAmount(value, prevYear)

    override fun adjustAmount(value: Amount, prevYear: YearlyDetail): Amount {
        val assetRec = RecFinder.findAssetRec(assetIdent, prevYear)
        return if (assetRec == null || assetRec.finalBalance() < 1.0) 0.0
        else value
    }
}