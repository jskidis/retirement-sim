package expense

import Amount
import Name
import Year
import config.AmountConfig
import moneyFormat
import progression.AmountToRecProvider
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxableAmounts

data class ExpenseRec(
    val year: Year,
    val config: ExpenseConfig,
    val amount: Amount,
    val taxDeductions: TaxableAmounts,
) {
    val deductionsStr = if (taxDeductions.hasAmounts()) ", deductions=$taxDeductions" else ""
    override fun toString(): String =
        "($config=${moneyFormat.format(amount)}$deductionsStr)"
}

data class ExpenseConfig(
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile,
): AmountConfig {
    override fun toString(): String = "$person-$name"
}

data class ExpenseConfigProgression(
    val config: ExpenseConfig,
    val progression: Progression<ExpenseRec>,
)

open class ExpenseRecProvider(val config: ExpenseConfig)
    : AmountToRecProvider<ExpenseRec> {

    override fun createRecord(value: Amount, year: Year) = ExpenseRec(
        year = year,
        config = config,
        amount = value,
        taxDeductions = config.taxabilityProfile.calcTaxable(config.person, value)
    )
}

