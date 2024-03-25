package asset
import Amount
import Name
import config.AmountConfig
import progression.Progression
import tax.TaxabilityProfile
import tax.UnusedProfile

data class AssetConfig(
    override val name: Name,
    override val person: Name,
    // Taxability of gains
    override val taxabilityProfile: TaxabilityProfile = UnusedProfile(),
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
        balance: Amount, attribs: PortfolAttribs, config: AssetConfig, gaussianRnd: Double,
    ): AssetChange
}

interface GrossGainsCalc {
    fun calcGrossGains(
        balance: Amount, attribs: PortfolAttribs, gaussianRnd: Double,
    ): Amount = balance * (attribs.mean + (attribs.stdDev * gaussianRnd))
}

