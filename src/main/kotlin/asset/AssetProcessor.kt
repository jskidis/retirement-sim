package asset

import YearlyDetail
import config.SimConfig

object AssetProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?)
        : List<AssetRec> {

        val assets: List<AssetProgression> =
            config.household.jointAssets +
                config.household.members.people().flatMap { it.assets() }

        return assets.map {
            it.determineNext(prevYear)
        }.filter { it.retainRec() }
    }
}