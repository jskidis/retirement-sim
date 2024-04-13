package config.sample

import Amount
import RecIdentifier
import config.DependentConfigBuilder
import config.Person
import expense.BasicExpenseProgression
import expense.ExpenseProgression
import inflation.StdInflationAmountAdjuster
import medical.*
import tax.NonDeductProfile

object Suzy: DependentConfigBuilder {
    val expenseStart: Amount = 20000.0

    override fun expenses(person: Person): List<ExpenseProgression> {
        return listOf(
            BasicExpenseProgression(
                ident = RecIdentifier(name = "Expenses", person = person.name),
                startAmount = expenseStart,
                taxabilityProfile = NonDeductProfile(),
                adjusters = listOf(StdInflationAmountAdjuster())
            )
        )
    }

    override fun medInsurance(person: Person): List<MedInsuranceProgression> {
        return listOf(
            DependantInsFixedYearProgression(2030),
            EmployerInsPremProgression(
                employments = listOf(Jane.janeEmpConfig),
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