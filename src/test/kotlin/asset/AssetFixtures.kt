import asset.AssetCalcValuesRec
import asset.AssetConfig
import asset.AssetRec
import asset.NoMinMaxBalProvider
import tax.NonTaxableProfile
import tax.TaxableAmounts

fun assetConfigFixture(assetName: Name, person: Name) = AssetConfig(
    assetName, person, NonTaxableProfile(), AssetType.INVEST, NoMinMaxBalProvider())

fun assetRecFixture(assetConfig: AssetConfig, startBal: Amount = 0.0, endBal: Amount = 0.0) =
    AssetRec(
        config = assetConfig,
        startBal = startBal,
        taxable = TaxableAmounts(assetConfig.person),
        calcValues = AssetCalcValuesRec(finalBal = endBal)
    )


