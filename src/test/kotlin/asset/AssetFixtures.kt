import asset.*
import progression.Progression
import tax.NonTaxableProfile
import tax.TaxabilityProfile
import tax.TaxabilityProfileFixture

fun assetConfigFixture(
    assetName: Name = "Asset",
    person: Name = "Person",
    taxProfile: TaxabilityProfile = NonTaxableProfile(),
    attributesSet: List<YearlyAssetAttributes> = ArrayList()
) = AssetConfig(
    AssetType.INVEST, assetName, person, taxProfile, attributesSet)

fun assetRecFixture(
    assetConfig: AssetConfig = assetConfigFixture(assetName = "Asset Name", person = "Person"),
    startBal: Amount = 0.0, endBal: Amount = 0.0,
    gains: Amount = 0.0, taxProfile: TaxabilityProfile = NonTaxableProfile())
: AssetRec {

    val assetRec = AssetRec(
        config = assetConfig,
        startBal = startBal,
        gains = SimpleAssetChange("Gain", gains)
    )

    assetRec.calcValues = AssetCalcValuesRec(finalBal = endBal, totalGains = gains,
        taxable = taxProfile.calcTaxable("", gains))
    return assetRec
}

fun assetConfigProgressFixture(
    name: Name = "Asset",
    person: Name = "Person",
    startBal: Amount = 0.0,
    gains: Amount = 0.0,
) : AssetConfigProgression {

    val config = AssetConfig(
        AssetType.CASH, name, person, TaxabilityProfileFixture()
    )

    return AssetConfigProgression(
        config, AssetProgressionFixture(startBal, gains, config)
    )
}

class AssetProgressionFixture(
    val startBal: Amount,
    val gains: Amount,
    val assetConfig: AssetConfig,
) : Progression<AssetRec> {

    override fun determineNext(prevYear: YearlyDetail?) = assetRecFixture(
        startBal = startBal,
        gains = gains,
        assetConfig = assetConfig
    )
}
