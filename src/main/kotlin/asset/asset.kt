package asset
import Amount
import Name
import Year
import YearlyDetail
import config.AmountConfig
import progression.Progression
import tax.TaxabilityProfile

enum class AssetType {
    CASH, INVEST, TIRA, ROTH
}

data class AssetConfig(
    val type: AssetType,
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile,
) : AmountConfig {

    override fun toString(): String = "$person-$name"
}

data class AssetConfigProgression(
    val config: AssetConfig,
    val progression: Progression<AssetRec>,
    val withdrawDepositHandler: WithdrawDepositHandler,
)

data class YearlyAssetAttributes(
    val startYear: Year,
    val attributes: PortfolAttribs,
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

