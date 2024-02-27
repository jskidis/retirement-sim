package income

import Amount
import Name
import Year
import config.AmountConfig
import progression.AmountToRecProvider
import progression.NullableProgression
import tax.TaxabilityProfile
import tax.TaxableAmounts
import util.moneyFormat

data class IncomeRec(
    val year: Year,
    val config: IncomeConfig,
    val amount: Amount,
    val taxableIncome: TaxableAmounts,
) {
    val taxableStr = if (taxableIncome.hasAmounts()) ", taxable=$taxableIncome" else ""
    override fun toString(): String =
        "($config=${moneyFormat.format(amount)}$taxableStr)"
}

data class IncomeConfig(
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile,
) : AmountConfig {
    override fun toString(): String = "$person-$name"
}

data class IncomeConfigProgression(
    val config: IncomeConfig,
    val progression: NullableProgression<IncomeRec>,
)

open class IncomeRecProvider(val config: IncomeConfig)
    : AmountToRecProvider<IncomeRec> {

    override fun createRecord(value: Amount, year: Year) = IncomeRec(
        year = year,
        config = config,
        amount = value,
        taxableIncome = config.taxabilityProfile.calcTaxable(config.person, value))
}
