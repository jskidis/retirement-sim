package asset

import Amount
import RecIdentifier
import Year
import YearlyDetail
import cashflow.CashFlowEventHandler
import progression.PrevRecProviderProgression
import util.RandomizerFactory
import util.currentDate

open class AssetProgression(
    val ident: RecIdentifier,
    val startBalance: Amount,
    val gainCreator: AssetGainCreator,
    val cashflowEvents: List<CashFlowEventHandler> = ArrayList(),
) : PrevRecProviderProgression<AssetRec> {

    override fun previousRec(prevYear: YearlyDetail): AssetRec? = findAssetRec(prevYear)

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

    fun findAssetRec(yearDetail: YearlyDetail): AssetRec? =
        yearDetail.assets.find {
            it.ident.person == ident.person && it.ident.name == ident.name
        }

    fun buildRec(year: Year, balance: Amount, unrealized: Amount, roiGaussRnd: Double): AssetRec {
        return AssetRec(
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
    }

    open fun getROIRandom(prevYear: YearlyDetail?): Double =
        RandomizerFactory.getROIRandom(prevYear)
}
