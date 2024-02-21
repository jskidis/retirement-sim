import asset.*
import progression.Progression
import tax.NonTaxableProfile
import tax.TaxabilityProfile
import tax.TaxabilityProfileFixture
import tax.TaxableAmounts

fun assetConfigFixture(assetName: Name, person: Name) = AssetConfig(
    assetName, person, NonTaxableProfile(), AssetType.INVEST, NoMinMaxBalProvider())

fun assetRecFixture(
    assetConfig: AssetConfig = assetConfigFixture(assetName = "Asset Name", person = "Person"),
    startBal: Amount = 0.0, endBal: Amount = 0.0,
    gains: Amount = 0.0, taxProfile: TaxabilityProfile = NonTaxableProfile(),
) =
    AssetRec(
        config = assetConfig,
        startBal = startBal,
        taxable = taxProfile.calcTaxable(assetConfig.person, gains),
        calcValues = AssetCalcValuesRec(finalBal = endBal, totalGains = gains)
    )

fun assetCfgProgessFixture(name: Name, person: Name, startBal: Amount, gains: List<Amount>)
    : AssetConfigProgression {

    val config = AssetConfig(
        name, person,
        TaxabilityProfileFixture(), AssetType.INVEST, NoMinMaxBalProvider()
    )

    return AssetConfigProgression(
        config, AssetProgressionFixture(startBal, gains, config)
    )
}

class AssetProgressionFixture(
    val startBal: Amount,
    val gainAmnts: List<Amount>,
    val assetCfg: AssetConfig,
) : Progression<AssetRec> {

    override fun determineNext(prevYear: YearlyDetail?) = AssetRec(
        config = assetCfg,
        startBal = startBal,
        taxable = TaxableAmounts(assetCfg.name),
        gains = gainAmnts.mapIndexed { idx, amount ->
            AssetGain("Gain $idx", amount)
        }
    )
}

data class RORProviderFixture(val mean: Rate, val stdDev: Rate) : AssetRORProvider {
    override fun determineRate(prevYear: YearlyDetail?): Rate =
        mean + (stdDev * (prevYear?.rorRndGaussian ?: 0.0))
}




