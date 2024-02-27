package income

import Amount
import AmountRec
import Name
import Year
import config.AmountConfig
import progression.AmountToRecProvider
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxableAmounts
import util.moneyFormat

data class IncomeRec(
    val year: Year,
    val config: IncomeConfig,
    val amount: Amount,
    val taxableIncome: TaxableAmounts,
): AmountRec {

    override fun year(): Year  = year
    override fun config(): AmountConfig = config
    override fun taxable(): TaxableAmounts = taxableIncome
    override fun retainRec(): Boolean = amount != 0.0

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
    val progression: Progression<IncomeRec>,
)

open class IncomeRecProvider(val config: IncomeConfig)
    : AmountToRecProvider<IncomeRec> {

    override fun createRecord(value: Amount, year: Year) = IncomeRec(
        year = year,
        config = config,
        amount = value,
        taxableIncome = config.taxabilityProfile.calcTaxable(config.person, value))
}
