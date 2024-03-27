package asset

import Amount
import Year
import YearlyDetail
import progression.PrevRecProviderProgression
import util.RandomizerFactory
import util.RoiRandomProvider
import util.YearBasedConfig
import util.currentDate

open class AssetProgression(
    val startBalance: Amount,
    val config: AssetConfig,
    val gainCreator: AssetGainCreator,
    val requiredDistHandler: RequiredDistHandler = NullRequestDist(),
    val attributesSet: YearBasedConfig<PortfolAttribs> = YearBasedConfig(listOf()),
) : PrevRecProviderProgression<AssetRec>, RoiRandomProvider {

    override fun previousRec(prevYear: YearlyDetail): AssetRec? =
        prevYear.assets.find {
            it.config.person == config.person && it.config.name == config.name
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
            roiGaussRnd = getRoiRandom(prevYear)
        )

    fun buildRec(year: Year, balance: Amount, unrealized: Amount, roiGaussRnd: Double): AssetRec {
        val attributes = attributesSet.getConfigForYear(year)

        val assetRec = AssetRec(
            year = year,
            config = config,
            startBal = balance,
            startUnrealized = unrealized,
            gains = gainCreator.createGain(balance, attributes, config, roiGaussRnd))

        val reqDistribution = requiredDistHandler.generateDistribution(balance, year)
        if (reqDistribution != null) assetRec.tributions.add(reqDistribution)

        return assetRec
    }

    override fun getRoiRandom(prevYear: YearlyDetail?): Double =
        RandomizerFactory.getRoiRandom(prevYear)
}
