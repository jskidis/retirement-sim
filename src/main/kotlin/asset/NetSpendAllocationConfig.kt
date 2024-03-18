package asset

data class NetSpendAllocationConfig(
    val withdrawlOrder: List<AssetConfigProgression>,
    val depositOrder: List<AssetConfigProgression>
)
