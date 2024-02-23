package config.sample

import config.DependentConfigBuilder
import config.Person
import expense.BasicExpenseProgression
import expense.ExpenseConfig
import expense.ExpenseConfigProgression
import inflation.StdInflationAmountAdjuster
import tax.NonTaxableProfile

object Jonny : DependentConfigBuilder {
    override fun expenses(person: Person): List<ExpenseConfigProgression> {
        val config = ExpenseConfig(
            name = "Expenses", person = person.name,
            taxabilityProfile = NonTaxableProfile()
        )
        return listOf(
            ExpenseConfigProgression(
                config = config,
                progression = BasicExpenseProgression(
                    startAmount = Smiths.jonnyExpStart,
                    config = config,
                    adjusters = listOf(StdInflationAmountAdjuster())
                )
            )
        )
    }
}