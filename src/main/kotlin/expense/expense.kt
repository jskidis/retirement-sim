package expense

import Amount
import AmountRec
import Name
import Year
import config.AmountConfig
import progression.AmountToRecProvider
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxableAmounts
import tax.UnusedProfile
import toJsonStr

data class ExpenseRec(
    val year: Year,
    val config: ExpenseConfig,
    val amount: Amount,
    val taxDeductions: TaxableAmounts,
): AmountRec {

    override fun year(): Year  = year
    override fun config(): AmountConfig = config
    override fun amount(): Amount  = amount
    override fun taxable(): TaxableAmounts = taxDeductions
    override fun retainRec(): Boolean = amount != 0.0

    override fun toString(): String = toJsonStr()
}

data class ExpenseConfig(
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile = UnusedProfile(),
): AmountConfig {
    override fun toString(): String = toJsonStr()
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


