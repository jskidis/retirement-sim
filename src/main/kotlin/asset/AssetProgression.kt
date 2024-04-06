package asset

import Amount
import RecIdentifier
import Year
import YearlyDetail
import progression.PrevRecProviderProgression
import util.RandomizerFactory
import util.currentDate

open class AssetProgression(
    val ident: RecIdentifier,
    val startBalance: Amount,
    val gainCreator: AssetGainCreator,
    val requiredDistHandler: RequiredDistHandler = NullRequestDist(),
) : PrevRecProviderProgression<AssetRec> {

    override fun previousRec(prevYear: YearlyDetail): AssetRec? =
        prevYear.assets.find {
            it.ident.person == ident.person && it.ident.name == ident.name
        }

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
            roiGaussRnd = getROIRandom(prevYear)
        )

    fun buildRec(year: Year, balance: Amount, unrealized: Amount, roiGaussRnd: Double): AssetRec {
        val assetRec = AssetRec(
            year = year,
            ident = ident,
            startBal = balance,
            startUnrealized = unrealized,
            gains = gainCreator.createGain(
                year = year,
                person = ident.person,
                balance = balance,
                gaussianRnd = roiGaussRnd
            )
        )

        val reqDistribution = requiredDistHandler.generateDistribution(balance, year)
        if (reqDistribution != null) assetRec.tributions.add(reqDistribution)

        return assetRec
    }

    open fun getROIRandom(prevYear: YearlyDetail?): Double =
        RandomizerFactory.getROIRandom(prevYear)
}
