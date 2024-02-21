enum class AssetType {
    CASH, TIRA, ROTH, INVEST
}

data class AssetNetContribution(
    val name: Name,
    val amount: Amount,
) {
    override fun toString(): String =
        "($name: ${moneyFormat.format(amount)})"
}

typealias AssetGain = AssetNetContribution
