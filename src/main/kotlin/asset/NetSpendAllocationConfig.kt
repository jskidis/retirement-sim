package asset

data class NetSpendAllocationConfig(
    val withdrawOrder: List<AssetConfigProgression>,
    val depositOrder: List<AssetConfigProgression>
)
