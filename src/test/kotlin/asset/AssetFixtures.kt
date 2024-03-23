package asset

import Amount
import Name
import Year
import YearlyDetail
import progression.PrevYearProgression
import tax.NonTaxableProfile
import tax.TaxabilityProfile
import tax.TaxabilityProfileFixture

fun assetConfigFixture(
    assetName: Name = "Asset",
    person: Name = "Person",
    taxProfile: TaxabilityProfile = NonTaxableProfile(),
) = AssetConfig(
    assetName, person, taxProfile
)

fun assetRecFixture(
    year: Year =  2024,
    assetConfig: AssetConfig = assetConfigFixture(assetName = "Asset Name", person = "Person"),
    startBal: Amount = 0.0,
    gains: Amount = 0.0,
    startUnrealized: Amount = 0.0,
    taxProfile: TaxabilityProfile = NonTaxableProfile(),
) = AssetRec(
        year = year,
        config = assetConfig,
        startBal = startBal,
        startUnrealized = startUnrealized,
        gains = AssetChange("Gain", gains,
            taxProfile.calcTaxable(assetConfig.person, gains))
    )

fun assetConfigProgressFixture(
    name: Name = "Asset",
    person: Name = "Person",
    startBal: Amount = 0.0,
    gains: Amount = 0.0,
    spendAllocHandler: SpendAllocHandler = BasicSpendAlloc()
) : AssetConfigProgression {

    val config = AssetConfig(
        name, person, TaxabilityProfileFixture()
    )

    return AssetConfigProgression(
        config = config,
        progression = AssetProgressionFixture(startBal, gains, config),
        spendAllocHandler = spendAllocHandler
    )
}

class AssetProgressionFixture(
    val startBal: Amount,
    val gains: Amount,
    val assetConfig: AssetConfig,
) : PrevYearProgression<AssetRec> {

    override fun determineFromPrev(prevYear: YearlyDetail?) = assetRecFixture(
        startBal = startBal,
        gains = gains,
        assetConfig = assetConfig,
    )
}
