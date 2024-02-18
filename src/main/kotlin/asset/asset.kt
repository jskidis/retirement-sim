import tax.TaxableAmounts

data class AssetRec(
    val startBal: Amount,
    val gains: Amount,
    val taxable: TaxableAmounts,
    val assetName: String,
    val person: Name,
    var contributions: Amount = 0.0,
    var withdrawls: Amount = 0.0,
)

