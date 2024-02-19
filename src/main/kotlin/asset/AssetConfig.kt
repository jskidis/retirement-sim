package asset

import AssetType
import Name
import Year
import config.AmountConfig
import progression.Progression
import tax.TaxabilityProfile

data class AssetConfig(
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile,
    val type: AssetType,
    val minMaxProvider: AssetMinMaxBalProvider,
    val assetCompMap: List<YearlyAssetComposition> = ArrayList(),
) : AmountConfig {

    fun determineComposition(year: Year): List<AssetComposition> =
        assetCompMap.findLast { it.startYear <= year }
            ?.composition
            ?: throw RuntimeException("Unable to find asset composition for year:$year for asset:$name")
}

data class AssetConfigProgression(
    val config: AssetConfig,
    val progression: Progression<AssetRec>,
)

data class AssetComposition(
    val name: Name,
    val pct: Double,
    val rorProvider: AssetRORProvider,
)

data class YearlyAssetComposition(
    val startYear: Year,
    val composition: List<AssetComposition> = ArrayList(),
)