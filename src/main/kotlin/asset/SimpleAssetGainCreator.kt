package asset

import Amount
import Name
import Year
import tax.TaxabilityProfile
import util.YearBasedConfig

open class SimpleAssetGainCreator(
    val taxability: TaxabilityProfile,
    val attributesSet: YearBasedConfig<PortfolioAttribs>
) : AssetGainCreator, GrossGainsCalc {

    override fun createGain(year: Year, person: Name, balance: Amount, gaussianRnd: Double)
    : AssetChange {
        val attributes = attributesSet.getConfigForYear(year)
        val gainAmount = calcGrossGains(balance, attributes, gaussianRnd)
        val taxable = taxability.calcTaxable(person, gainAmount)
        return AssetChange(attributes.name, gainAmount, taxable)
    }
}