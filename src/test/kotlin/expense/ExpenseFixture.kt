package expense

import Amount
import Name
import YearlyDetail
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxabilityProfileFixture
import tax.TaxableAmounts

fun expenseConfigFixture(
    name: Name = "Expense",
    person: Name = "Person",
) = ExpenseConfig(name, person, TaxabilityProfileFixture())

fun expenseRecFixture(
    name: Name = "Expense",
    person: Name = "Person",
    amount: Amount = 0.0,
) = ExpenseRec(expenseConfigFixture(name, person), amount, TaxableAmounts(person))

fun expenseRecFixture(
    name: Name = "Expense",
    person: Name = "Person",
    amount: Amount = 0.0,
    taxProfile: TaxabilityProfile,
) = ExpenseRec(
    ExpenseConfig(name, person, taxProfile),
    amount,
    taxProfile.calcTaxable(name, amount)
)

fun expenseCfgProgessFixture(
    name: Name = "Expense",
    person: Name = "Person",
    amount: Amount = 0.0,
): ExpenseConfigProgression {
    val config = ExpenseConfig(name, person, TaxabilityProfileFixture())
    return ExpenseConfigProgression(
        config, ExpenseProgressionFixture(amount, config)
    )
}

class ExpenseProgressionFixture(val amount: Double, val expenseCfg: ExpenseConfig)
    : Progression<ExpenseRec> {

    override fun determineNext(prevYear: YearlyDetail?) = ExpenseRec(
        config = expenseCfg,
        amount = amount,
        taxDeductions = TaxableAmounts(expenseCfg.name)
    )
}
