package income

import Amount
import Name
import Year
import YearlyDetail
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxabilityProfileFixture
import tax.TaxableAmounts

fun incomeConfigFixture(
    name: Name = "Income",
    person: Name = "Person",
) = IncomeConfig(name, person, TaxabilityProfileFixture())

fun incomeRecFixture(
    year: Year = 2024,
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0,
) = IncomeRec(
    year = year,
    config = incomeConfigFixture(name = name, person = person),
    amount = amount,
    taxableIncome = TaxableAmounts(person))

fun incomeRecFixture(
    year: Year = 2024,
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0,
    taxProfile: TaxabilityProfile
) = IncomeRec(
    year = year,
    config = IncomeConfig(name = name, person = person, taxabilityProfile = taxProfile),
    amount = amount,
    taxableIncome = taxProfile.calcTaxable(name, amount))

fun incomeCfgProgessFixture(
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0
): IncomeConfigProgression {
    val config = IncomeConfig(name, person, TaxabilityProfileFixture())

    return IncomeConfigProgression(
        config, IncomeProgressionFixture(amount, config)
    )
}

class IncomeProgressionFixture(val amount: Double, val expenseCfg: IncomeConfig)
    : Progression<IncomeRec> {

    override fun determineNext(prevYear: YearlyDetail?) = IncomeRec(
        year = prevYear?.year ?:2024,
        config = expenseCfg,
        amount = amount,
        taxableIncome = TaxableAmounts(expenseCfg.name)
    )
}
