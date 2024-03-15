package socsec

import Amount
import AmountRec
import Name
import Year
import config.AmountConfig
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxableAmounts
import util.moneyFormat

data class SSBenefitRec(
    val year: Year,
    val config: SSBenefitConfig,
    val amount: Amount,
    val taxableAmount: TaxableAmounts,
): AmountRec {

    override fun year(): Year  = year
    override fun config(): AmountConfig = config
    override fun taxable(): TaxableAmounts = taxableAmount
    override fun retainRec(): Boolean = amount != 0.0

    val taxableStr = if (taxableAmount.hasAmounts()) ", taxable=$taxableAmount" else ""
    override fun toString(): String =
        "($config=${moneyFormat.format(amount)}$taxableStr)"
}

data class SSBenefitConfig(
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile,
) : AmountConfig {
    override fun toString(): String = "$person-$name"
}

data class SSBenefitConfigProgression(
    val config: SSBenefitConfig,
    val progression: Progression<SSBenefitRec>,
)
