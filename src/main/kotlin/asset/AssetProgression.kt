package asset

import Amount
import YearlyDetail
import currentDate
import progression.Progression

abstract class AssetProgression(
    val startBalance: Amount,
    val config: AssetConfig,
) : Progression<AssetRec>, AssetGainCreator {

    override fun determineNext(prevYear: YearlyDetail?): AssetRec {
        val year =
            if (prevYear == null) currentDate.year
            else prevYear.year + 1

        val attributes = config.retrieveAttributesByYear(year)

        val balance =
            if (prevYear == null) startBalance
            else previousRec(prevYear)?.finalBalance() ?: startBalance

        return AssetRec(
            year = year,
            config = config,
            startBal = balance,
            gains = createGain(balance, attributes, config, prevYear))
    }

    fun previousRec(prevYear: YearlyDetail): AssetRec? =
        prevYear.assets.find {
            it.config.person == config.person && it.config.name == config.name
        }
}

open class SimpleAssetProgression(startBalance: Amount, config: AssetConfig)
    : AssetProgression(startBalance, config), AssetGainCreator by SimpleAssetGainCreator()

open class TaxableInvestProgression(
    startBalance: Amount,
    config: AssetConfig,
    qualDivRatio: Double = 0.80,
    regTaxOnGainsPct: Double = 0.10,
    ltTaxOnGainsPct: Double = 0.10,
) : AssetProgression(startBalance, config),
    AssetGainCreator by TaxableInvestGainCreator(qualDivRatio, regTaxOnGainsPct, ltTaxOnGainsPct)
