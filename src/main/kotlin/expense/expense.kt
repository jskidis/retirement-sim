package expense

import Amount
import Name
import config.AmountConfig
import moneyFormat
import progression.AmountToRecProvider
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxableAmounts

data class ExpenseRec(
    val config: ExpenseConfig,
    val amount: Amount,
    val taxDeductions: TaxableAmounts,
) {
    override fun toString(): String =
        "($config = ${moneyFormat.format(amount)}, deductions=$taxDeductions"
}

data class ExpenseConfig(
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile,
): AmountConfig {
    override fun toString(): String = "$person:$name"
}

data class ExpenseConfigProgression(
    val config: ExpenseConfig,
    val progression: Progression<ExpenseRec>,
)

open class ExpenseRecProvider(val config: ExpenseConfig)
    : AmountToRecProvider<ExpenseRec> {

    override fun createRecord(value: Amount) = ExpenseRec(
        config = config, amount = value,
        taxDeductions = config.taxabilityProfile.calcTaxable(config.person, value)
    )
}

