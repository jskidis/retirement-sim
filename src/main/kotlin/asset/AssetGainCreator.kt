package asset

import Amount
import YearlyDetail

interface AssetGainCreator {
    fun createGain(
        balance: Amount, attribs: PortfolAttribs, config: AssetConfig, prevYear: YearlyDetail?,
    ): AssetChange

    fun grossGainAmount(
        balance: Amount, attribs: PortfolAttribs, config: AssetConfig, prevYear: YearlyDetail?,
    ): Amount = balance * (attribs.mean + (attribs.stdDev * (prevYear?.rorRndGaussian ?: 0.0)))
}

open class SimpleAssetGainCreator : AssetGainCreator {
    override fun createGain(
        balance: Amount, attribs: PortfolAttribs, config: AssetConfig, prevYear: YearlyDetail?,
    ): AssetChange {
        val gainAmount = grossGainAmount(balance, attribs, config, prevYear)
        val taxable = config.taxabilityProfile.calcTaxable(config.person, gainAmount)
        return SimpleAssetChange(attribs.name, gainAmount, taxable)
    }
}

open class TaxableInvestGainCreator : SimpleAssetGainCreator()


