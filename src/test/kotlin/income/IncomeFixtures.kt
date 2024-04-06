package income

import Amount
import Name
import RecIdentifier
import Year
import tax.TaxabilityProfile
import tax.TaxableAmounts
import tax.WageTaxableProfile
import util.currentDate

fun incomeRecFixture(
    year: Year = currentDate.year + 1,
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0,
) = IncomeRec(
    year = year,
    ident = RecIdentifier(name = name, person = person),
    baseAmount = amount,
    taxableIncome = TaxableAmounts(person))

fun incomeRecFixture(
    year: Year = currentDate.year + 1,
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0,
    taxProfile: TaxabilityProfile
) = IncomeRec(
    year = year,
    ident = RecIdentifier(name = name, person = person),
    baseAmount = amount,
    taxableIncome = taxProfile.calcTaxable(name, amount))

fun incomeProgressionFixture(
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0
): IncomeProgression {
    return BasicIncomeProgression(
        ident = RecIdentifier(name, person),
        startAmount = amount,
        taxabilityProfile = WageTaxableProfile(),
        adjusters = listOf()
    )
}
