package asset

import YearlyDetail
import config.SimConfig

object AssetProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?, currYear: YearlyDetail)
    : List<AssetRec> {
        val assets: List<AssetConfigProgression> = config.jointAssets +
            config.householdMembers.people().flatMap { it.assets() }

        return assets.map {
            val assetRec = it.progression.determineNext(prevYear)
            assetRec.calcValues = AssetCalcValuesRec.create(assetRec, currYear)
            assetRec
        }
    }
}