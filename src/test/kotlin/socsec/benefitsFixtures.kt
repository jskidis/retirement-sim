package socsec

import Amount
import Name
import Year
import tax.TaxabilityProfileFixture
import tax.TaxableAmounts

fun benefitsConfigFixture(
    name: Name = "Expense",
    person: Name = "Person",
) = SSBenefitConfig(name, person, TaxabilityProfileFixture())

fun benefitsRecFixture(
    year: Year = 2024,
    name: Name = "Benefit",
    person: Name = "Person",
    amount: Amount = 0.0,
) = SSBenefitRec(
    year = year,
    config = benefitsConfigFixture(name, person),
    amount = amount,
    taxableAmount = TaxableAmounts(person)
)

