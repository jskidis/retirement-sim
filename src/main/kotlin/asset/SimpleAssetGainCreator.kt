package asset

import Amount
import YearlyDetail

open class SimpleAssetGainCreator : AssetGainCreator, GrossGainsCalc {
    override fun createGain(
        balance: Amount, attribs: PortfolAttribs, config: AssetConfig, prevYear: YearlyDetail?,
    ): AssetChange {
        val gainAmount = calcGrossGains(balance, attribs, prevYear)
        val taxable = config.taxabilityProfile.calcTaxable(config.person, gainAmount)
        return SimpleAssetChange(attribs.name, gainAmount, taxable)
    }
}