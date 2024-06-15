package asset

import Amount
import Name
import Rate
import Year
import tax.NonTaxableProfile
import tax.TaxabilityProfile

class FixedPctGainCreator(
    val fixedPct: Rate,
    val gainName: Name,
    val taxability: TaxabilityProfile = NonTaxableProfile(),
) : AssetGainCreator {

    override fun createGain(
        year: Year, person: Name, balance: Amount, gaussianRnd: Double,
    ): AssetChange {
        val gainAmount = fixedPct * balance
        return AssetChange(gainName, gainAmount, taxability.calcTaxable(person, gainAmount))
    }
}