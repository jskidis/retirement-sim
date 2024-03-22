package asset

import Amount
import YearlyDetail
import progression.Progression
import util.YearBasedConfig
import util.yearFromPrevYearDetail

open class AssetProgression(
    val startBalance: Amount,
    val config: AssetConfig,
    val gainCreator: AssetGainCreator,
    val requiredDistHandler: RequiredDistHandler = NullRequestDist(),
    val attributesSet: YearBasedConfig<PortfolAttribs> = YearBasedConfig(listOf()),

) : Progression<AssetRec> {

    override fun determineNext(prevYear: YearlyDetail?): AssetRec {
        val year = yearFromPrevYearDetail(prevYear)
        val attributes = attributesSet.getConfigForYear(year)

        val prevRec = if(prevYear == null) null else previousRec(prevYear)

        val balance =
            if (prevYear == null) startBalance
            else prevRec?.finalBalance() ?: 0.0

        val unrealized = prevRec?.totalUnrealized() ?: 0.0

        val assetRec = AssetRec(
            year = year,
            config = config,
            startBal = balance,
            startUnrealized = unrealized,
            gains = gainCreator.createGain(balance, attributes, config, prevYear))

        val reqDistribution = requiredDistHandler.generateDistribution(balance, year)
        if (reqDistribution != null) assetRec.tributions.add(reqDistribution)

        return assetRec
    }

    fun previousRec(prevYear: YearlyDetail): AssetRec? =
        prevYear.assets.find {
            it.config.person == config.person && it.config.name == config.name
        }
}
