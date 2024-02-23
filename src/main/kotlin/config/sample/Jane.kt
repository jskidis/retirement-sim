package config.sample

import config.ParentConfigBuilder
import config.Person
import expense.AgeBasedExpenseAdjuster
import expense.BasicExpenseProgression
import expense.ExpenseConfig
import expense.ExpenseConfigProgression
import income.BasicIncomeProgression
import income.IncomeConfig
import income.IncomeConfigProgression
import inflation.StdInflationAmountAdjuster
import tax.NonTaxableProfile
import tax.WageTaxableProfile

object Jane : ParentConfigBuilder {
    override fun incomes(person: Person): List<IncomeConfigProgression> {
        val janeIncomeConfig = IncomeConfig(
            name = "BigCo", person = person.name,
            taxabilityProfile = WageTaxableProfile()
        )
        return listOf(
            IncomeConfigProgression(
                config = janeIncomeConfig,
                progression = BasicIncomeProgression(
                    startAmount = Smiths.janeIncStart,
                    config = janeIncomeConfig,
                    adjusters = listOf(StdInflationAmountAdjuster())
                )
            )
        )
    }

    override fun expenses(person: Person): List<ExpenseConfigProgression> {
        val janeExpenseConfig = ExpenseConfig(
            name = "Expenses", person = person.name,
            taxabilityProfile = NonTaxableProfile()
        )
        return listOf(
            ExpenseConfigProgression(
                config = janeExpenseConfig,
                progression = BasicExpenseProgression(
                    startAmount = Smiths.janeExpStart,
                    config = janeExpenseConfig,
                    adjusters = listOf(
                        StdInflationAmountAdjuster(),
                        AgeBasedExpenseAdjuster(person.birthYM)
                    )
                )
            )
        )
    }
}