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
    taxableAmounts: TaxableAmounts = TaxableAmounts(person)
) = StdIncomeRec(
    year = year,
    ident = RecIdentifier(name = name, person = person),
    amount = amount,
    taxableIncome = taxableAmounts)

fun incomeWithBonusRecFixture(
    year: Year = currentDate.year + 1,
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0,
    bonus: Amount = 0.0,
    taxableAmounts: TaxableAmounts = TaxableAmounts(person)
) = IncomeWithBonusRec(
    year = year,
    ident = RecIdentifier(name = name, person = person),
    baseAmount = amount,
    bonus = bonus,
    taxableIncome = taxableAmounts)

fun incomeRecFixture(
    year: Year = currentDate.year + 1,
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0,
    taxProfile: TaxabilityProfile
) = StdIncomeRec(
    year = year,
    ident = RecIdentifier(name = name, person = person),
    amount = amount,
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
