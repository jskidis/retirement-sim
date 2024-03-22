package asset
import Amount
import Name
import YearlyDetail
import config.AmountConfig
import progression.Progression
import tax.TaxabilityProfile

data class AssetConfig(
    override val name: Name,
    override val person: Name,
    // Taxability of gains
    override val taxabilityProfile: TaxabilityProfile,
) : AmountConfig {

    override fun toString(): String = "$person-$name"
}

data class AssetConfigProgression(
    val config: AssetConfig,
    val progression: Progression<AssetRec>,
    val spendAllocHandler: SpendAllocHandler,
)

interface AssetGainCreator {
    fun createGain(
        balance: Amount, attribs: PortfolAttribs, config: AssetConfig, prevYear: YearlyDetail?,
    ): AssetChange
}

interface GrossGainsCalc {
    fun calcGrossGains(
        balance: Amount, attribs: PortfolAttribs, prevYear: YearlyDetail?,
    ): Amount = balance * (attribs.mean + (attribs.stdDev * (prevYear?.rorRndGaussian ?: 0.0)))
}

