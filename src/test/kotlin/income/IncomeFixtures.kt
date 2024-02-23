package income

import Amount
import Name
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
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0,
) = IncomeRec(incomeConfigFixture(name, person), amount, TaxableAmounts(person))

fun incomeRecFixture(
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0,
    taxProfile: TaxabilityProfile
) = IncomeRec(IncomeConfig(name, person, taxProfile), amount, taxProfile.calcTaxable(name, amount))

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
        config = expenseCfg,
        amount = amount,
        taxableIncome = TaxableAmounts(expenseCfg.name)
    )
}
