enum class AssetType {
    CASH, TIRA, ROTH, INVEST
}

data class AssetNetContribution(
    val name: Name,
    val amount: Amount,
)

typealias AssetGain = AssetNetContribution
