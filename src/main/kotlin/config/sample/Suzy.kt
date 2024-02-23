package config.sample

import config.DependentConfigBuilder
import config.Person
import expense.ExpenseConfig
import expense.ExpenseConfigProgression
import expense.SCurveDecreasingExpense
import inflation.StdInflationAmountAdjuster
import tax.NonTaxableProfile

object Suzy: DependentConfigBuilder {
    override fun expenses(person: Person): List<ExpenseConfigProgression> {
        val config = ExpenseConfig(
            name = "Expenses", person = person.name,
            taxabilityProfile = NonTaxableProfile()
        )
        return listOf(
            ExpenseConfigProgression(
                config = config,
                progression = SCurveDecreasingExpense(
                    startAmount = Smiths.suzyExpStart,
                    startDecYear = Smiths.startYear,
                    numYears = 10,
                    config = config,
                    adjusters = listOf(StdInflationAmountAdjuster())
                )
            )
        )
    }
}