import asset.*
import progression.Progression
import tax.NonTaxableProfile
import tax.TaxabilityProfileFixture
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
): Progression<AssetRec> {

    override fun determineNext(prevYear: YearlyDetail?) = AssetRec(
        config = assetCfg,
        startBal = startBal,
        taxable = TaxableAmounts(assetCfg.name),
        gains = gainAmnts.mapIndexed { idx, amount ->
            AssetGain("Gain $idx", amount)
        }
    )
}




