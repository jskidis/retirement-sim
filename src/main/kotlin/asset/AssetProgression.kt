package asset

import Amount
import RecIdentifier
import Year
import YearlyDetail
import progression.PrevRecProviderProgression
import util.ROIRandom
import util.RandomizerFactory
import util.RecFinder
import util.currentDate

open class AssetProgression(
    val ident: RecIdentifier,
    val assetType: AssetType,
    val startBalance: Amount,
    val gainCreator: AssetGainCreator,
    val roiRandomizer: ROIRandom = RandomizerFactory,
) : PrevRecProviderProgression<AssetRec> {

    override fun previousRec(prevYear: YearlyDetail): AssetRec? =
        RecFinder.findAssetRec(ident, prevYear)

    override fun initialRec(): AssetRec =
        buildRec(
            year = currentDate.year,
            balance = startBalance,
            unrealized = 0.0,
            roiGaussRnd = 0.0
        )

    override fun nextRecFromPrev(prevYear: YearlyDetail): AssetRec =
        buildRec(
            year = prevYear.year + 1,
            balance = 0.0,
            unrealized = 0.0,
            roiGaussRnd = 0.0
        )

    override fun nextRecFromPrev(prevRec: AssetRec, prevYear: YearlyDetail): AssetRec =
        buildRec(
            year = prevYear.year + 1,
            balance = prevRec.finalBalance(),
            unrealized = prevRec.totalUnrealized(),
            roiGaussRnd = roiRandomizer.getROIRandom(prevYear)
        )

    fun buildRec(year: Year, balance: Amount, unrealized: Amount, roiGaussRnd: Double): AssetRec {
        return AssetRec(
            year = year,
            ident = ident,
            assetType = assetType,
            startBal = balance,
            startUnrealized = unrealized,
            gains = gainCreator.createGain(
                year = year,
                person = ident.person,
                balance = balance,
                gaussianRnd = roiGaussRnd
            )
        )
    }
}
