package config

import Name
import tax.NonTaxableProfile
import tax.TaxabilityProfile

//TODO Remove Once all amount configs have been removed
interface AmountConfig {
    val name: Name
    val person: Name
    val taxabilityProfile: TaxabilityProfile
    fun isValid(): Boolean = true
}

class SimpleAmountConfig(
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile = NonTaxableProfile()
): AmountConfig
