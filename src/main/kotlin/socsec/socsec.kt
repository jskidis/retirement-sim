package socsec

import Amount
import AmountRec
import Name
import Year
import config.AmountConfig
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxableAmounts
import tax.UnusedProfile
import toJsonStr

data class SSBenefitRec(
    val year: Year,
    val config: SSBenefitConfig,
    val amount: Amount,
    val taxableAmount: TaxableAmounts,
) : AmountRec {

    override fun year(): Year = year
    override fun config(): AmountConfig = config
    override fun amount(): Amount = amount

    override fun taxable(): TaxableAmounts = taxableAmount
    override fun retainRec(): Boolean = amount != 0.0

    override fun toString(): String = toJsonStr()
}

data class SSBenefitConfig(
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile = UnusedProfile(),
) : AmountConfig {
    override fun toString(): String = toJsonStr()
}

data class SSBenefitConfigProgression(
    val config: SSBenefitConfig,
    val progression: SSBenefitProgression,
)

interface SSBenefitProgression : Progression<SSBenefitRec>
