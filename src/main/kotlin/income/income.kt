package income

import Amount
import Name
import config.AmountConfig
import moneyFormat
import progression.AmountToRecProvider
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxableAmounts

data class IncomeRec(
    val config: IncomeConfig,
    val amount: Amount,
    val taxableIncome: TaxableAmounts,
) {
    override fun toString(): String =
        "($config = ${moneyFormat.format(amount)}, taxable=$taxableIncome"
}

data class IncomeConfig(
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile,
): AmountConfig {
    override fun toString(): String = "$person:$name"
}

data class IncomeConfigProgression(
    val config: IncomeConfig,
    val progression: Progression<IncomeRec>,
)

open class IncomeRecProvider(val config: IncomeConfig)
    : AmountToRecProvider<IncomeRec> {

    override fun createRecord(value: Amount) = IncomeRec(
        config = config, amount = value,
        taxableIncome = config.taxabilityProfile.calcTaxable(config.person, value)
    )
}
