package asset

import Amount
import YearlyDetail
import progression.Progression
import util.yearFromPrevYearDetail

open class AssetProgression(
    val startBalance: Amount,
    val config: AssetConfig,
    val gainCreator: AssetGainCreator,
) : Progression<AssetRec> {

    override fun determineNext(prevYear: YearlyDetail?): AssetRec {
        val year = yearFromPrevYearDetail(prevYear)
        val attributes = config.retrieveAttributesByYear(year)

        val balance =
            if (prevYear == null) startBalance
            else previousRec(prevYear)?.finalBalance() ?: startBalance

        return AssetRec(
            year = year,
            config = config,
            startBal = balance,
            gains = gainCreator.createGain(balance, attributes, config, prevYear))
    }

    fun previousRec(prevYear: YearlyDetail): AssetRec? =
        prevYear.assets.find {
            it.config.person == config.person && it.config.name == config.name
        }
}
