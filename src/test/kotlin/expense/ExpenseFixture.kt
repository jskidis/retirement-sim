package expense

import Amount
import Name
import RecIdentifier
import Year
import tax.NonTaxableProfile
import tax.TaxabilityProfile
import tax.TaxableAmounts
import util.currentDate

fun expenseRecFixture(
    year: Year = currentDate.year + 1,
    name: Name = "Expense",
    person: Name = "Person",
    amount: Amount = 0.0,
) = ExpenseRec(
    year = year,
    ident = RecIdentifier(name, person),
    amount = amount,
    taxDeductions = TaxableAmounts(person))

fun expenseRecFixture(
    year: Year = currentDate.year + 1,
    name: Name = "Expense",
    person: Name = "Person",
    amount: Amount = 0.0,
    taxProfile: TaxabilityProfile,
) = ExpenseRec(
    year = year,
    ident = RecIdentifier(name, person),
    amount = amount,
    taxDeductions = taxProfile.calcTaxable(name, amount)
)

fun expenseProgressionFixture(
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0
): ExpenseProgression {
    return BasicExpenseProgression(
        ident = RecIdentifier(name, person),
        startAmount = amount,
        taxabilityProfile = NonTaxableProfile(),
        adjusters = listOf()
    )
}

