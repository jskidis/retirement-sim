package asset

import Amount

open class SimpleAssetGainCreator : AssetGainCreator, GrossGainsCalc {
    override fun createGain(
        balance: Amount, attribs: PortfolAttribs, config: AssetConfig, gaussianRnd: Double,
    ): AssetChange {
        val gainAmount = calcGrossGains(balance, attribs, gaussianRnd)
        val taxable = config.taxabilityProfile.calcTaxable(config.person, gainAmount)
        return AssetChange(attribs.name, gainAmount, taxable)
    }
}