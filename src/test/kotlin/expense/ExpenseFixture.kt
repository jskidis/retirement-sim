package expense

import Amount
import Name
import Year
import YearlyDetail
import progression.AmountProviderProgression
import tax.TaxabilityProfile
import tax.TaxableAmounts

fun expenseConfigFixture(
    name: Name = "Expense",
    person: Name = "Person",
) = ExpenseConfig(name, person)

fun expenseRecFixture(
    year: Year = 2024,
    name: Name = "Expense",
    person: Name = "Person",
    amount: Amount = 0.0,
) = ExpenseRec(
    year = year,
    config = expenseConfigFixture(name, person),
    amount = amount,
    taxDeductions = TaxableAmounts(person))

fun expenseRecFixture(
    year: Year = 2024,
    name: Name = "Expense",
    person: Name = "Person",
    amount: Amount = 0.0,
    taxProfile: TaxabilityProfile,
) = ExpenseRec(
    year = year,
    config = ExpenseConfig(name, person, taxProfile),
    amount = amount,
    taxDeductions = taxProfile.calcTaxable(name, amount)
)

fun expenseCfgProgessFixture(
    name: Name = "Expense",
    person: Name = "Person",
    amount: Amount = 0.0,
): ExpenseConfigProgression {
    val config = ExpenseConfig(name, person)
    return ExpenseConfigProgression(
        config, ExpenseProgressionFixture(amount, config)
    )
}

class ExpenseProgressionFixture(val amount: Double, val expenseConfig: ExpenseConfig)
    : AmountProviderProgression<ExpenseRec> {

    override fun determineAmount(prevYear: YearlyDetail?): Amount = amount
    override fun createRecord(value: Amount, year: Year) = ExpenseRec(
        year = year,
        config = expenseConfig,
        amount = amount,
        taxDeductions = TaxableAmounts(expenseConfig.name)
    )
}
