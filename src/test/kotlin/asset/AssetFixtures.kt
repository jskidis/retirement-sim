package asset

import Amount
import Name
import Year
import YearlyDetail
import progression.Progression
import tax.NonTaxableProfile
import tax.TaxabilityProfile
import tax.TaxabilityProfileFixture

fun assetConfigFixture(
    assetName: Name = "Asset",
    person: Name = "Person",
    assetType: AssetType = AssetType.INVEST,
    taxProfile: TaxabilityProfile = NonTaxableProfile(),
    attributesSet: List<YearlyAssetAttributes> = ArrayList()
) = AssetConfig(
    assetType, assetName, person, taxProfile, attributesSet)

fun assetRecFixture(
    year: Year =  2024,
    assetConfig: AssetConfig = assetConfigFixture(assetName = "Asset Name", person = "Person"),
    startBal: Amount = 0.0,
    gains: Amount = 0.0,
    taxProfile: TaxabilityProfile = NonTaxableProfile()
) = AssetRec(
        year = year,
        config = assetConfig,
        startBal = startBal,
        gains = SimpleAssetChange("Gain", gains,
            taxProfile.calcTaxable(assetConfig.person, gains))
    )

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
