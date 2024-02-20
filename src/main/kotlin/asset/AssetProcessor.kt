package asset

import YearlyDetail
import config.MainConfig

object AssetProcessor {
    fun process(config: MainConfig, prevYear: YearlyDetail?)
    : List<AssetRec> {
        val assets: List<AssetConfigProgression> = config.jointAssets +
            config.householdMembers.people().flatMap { it.assets() }

        return assets.map {
            it.progression.determineNext(prevYear)
        }
    }
}