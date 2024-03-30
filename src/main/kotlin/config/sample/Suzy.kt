package config.sample

import Amount
import config.DependentConfigBuilder
import config.Person
import expense.ExpenseConfig
import expense.ExpenseConfigProgression
import expense.SCurveDecreasingExpense
import inflation.StdInflationAmountAdjuster
import medical.*
import tax.NonTaxableProfile

object Suzy: DependentConfigBuilder {
    val expenseStart: Amount = 20000.0

    override fun expenses(person: Person): List<ExpenseConfigProgression> {
        val config = ExpenseConfig(
            name = "Expenses", person = person.name,
            taxabilityProfile = NonTaxableProfile()
        )
        return listOf(
            ExpenseConfigProgression(
                config = config,
                progression = SCurveDecreasingExpense(
                    startAmount = expenseStart,
                    startDecYear = Smiths.startYear,
                    numYears = 10,
                    config = config,
                    adjusters = listOf(StdInflationAmountAdjuster())
                )
            )
        )
    }

    override fun medInsurance(person: Person): List<MedInsuranceProgression> {
        return listOf(
            DependantInsFixedYearProgression(2030),
            EmployerInsPremProgression(
                employments = Jane.employmentConfigs(Smiths.jane),
                relation = RelationToInsured.DEPENDANT
            ),
            MarketplacePremProgression(
                birthYM = person.birthYM,
                medalType = MPMedalType.SILVER,
                planType = MPPlanType.HMO,
                includeDental = true
            )
        )
    }
}