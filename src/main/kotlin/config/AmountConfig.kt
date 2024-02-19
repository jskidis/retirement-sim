package config

import Name
import tax.TaxabilityProfile

interface AmountConfig {
    val name: Name
    val person: Name
    val taxabilityProfile: TaxabilityProfile
    fun isValid(): Boolean = true
}