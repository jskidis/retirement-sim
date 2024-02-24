package asset

import Amount
import YearlyDetail
import currentDate
import progression.Progression

abstract class BasicAssetProgression(
    val startBalance: Amount,
    val config: AssetConfig,
) : Progression<AssetRec>, AssetGainCreator {

    override fun determineNext(prevYear: YearlyDetail?): AssetRec {
        val characteristics = config.retrieveAttributesByYear(
            if (prevYear == null) currentDate.year
            else prevYear.year + 1
        )

        val balance =
            if (prevYear == null) startBalance
            else previousRec(prevYear)?.calcValues?.finalBal ?: startBalance

        return AssetRec(
            config = config,
            startBal = startBalance,
            gains = createGain(balance, characteristics, config, prevYear))
    }

    private fun previousRec(prevYear: YearlyDetail): AssetRec? =
        prevYear.assets.find {
            it.config.person == config.person && it.config.name == config.name
        }
}

open class SimpleAssetProgression(startBalance: Amount, config: AssetConfig)
    : BasicAssetProgression(startBalance, config), AssetGainCreator by SimpleAssetGainCreator()

open class TaxableInvestProgression(startBalance: Amount, config: AssetConfig)
    : BasicAssetProgression(startBalance, config), AssetGainCreator by TaxableInvestGainCreator()
