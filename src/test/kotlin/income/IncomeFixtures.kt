package income

import Amount
import Name
import Year
import YearlyDetail
import progression.AmountProviderProgression
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
    baseAmount = amount,
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
    baseAmount = amount,
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

class IncomeProgressionFixture(val amount: Double, val incomeConfig: IncomeConfig)
    : AmountProviderProgression<IncomeRec> {

    override fun determineAmount(prevYear: YearlyDetail?): Amount = amount
    override fun createRecord(value: Amount, year: Year) = IncomeRec(
        year = year,
        config = incomeConfig,
        baseAmount = amount,
        taxableIncome = TaxableAmounts(person = incomeConfig.name,
            fed = amount, fedLTG = 0.0, state = amount, socSec = amount, medicare = amount)
    )
}
