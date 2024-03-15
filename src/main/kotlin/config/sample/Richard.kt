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
import progression.DateRangeAmountAdjuster
import socsec.FixedDateAmountSSBenefitProgression
import socsec.SSBenefitConfig
import socsec.SSBenefitConfigProgression
import tax.NonTaxableProfile
import tax.SSBenefitTaxableProfile
import tax.WageTaxableProfile

object Richard : ParentConfigBuilder {
    override fun incomes(person: Person): List<IncomeConfigProgression> {
        val incomeConfig = IncomeConfig(
            name = "PartTime", person = person.name,
            taxabilityProfile = WageTaxableProfile()
        )
        return listOf(
            IncomeConfigProgression(
                config = incomeConfig,
                progression = BasicIncomeProgression(
                    startAmount = Smiths.richardIncStart,
                    config = incomeConfig,
                    adjusters = listOf(
                        DateRangeAmountAdjuster(Smiths.richardEmploymentDate),
                        StdInflationAmountAdjuster())
                )
            )
        )
    }

    override fun expenses(person: Person): List<ExpenseConfigProgression> {
        val expenseConfig = ExpenseConfig(
            name = "Expenses", person = person.name,
            taxabilityProfile = NonTaxableProfile()
        )
        return listOf(
            ExpenseConfigProgression(
                config = expenseConfig,
                progression = BasicExpenseProgression(
                    startAmount = Smiths.richardExpStart,
                    config = expenseConfig,
                    adjusters = listOf(
                        StdInflationAmountAdjuster(),
                        AgeBasedExpenseAdjuster(person.birthYM)
                    )
                )
            )
        )
    }

    override fun benefits(person: Person): List<SSBenefitConfigProgression> {
        val benefitConfig = SSBenefitConfig(
            name = "Primary", person = person.name,
            taxabilityProfile = SSBenefitTaxableProfile()
        )
        return listOf(
            SSBenefitConfigProgression(
                config = benefitConfig,
                progression = FixedDateAmountSSBenefitProgression(
                    config = benefitConfig,
                    birthYM = person.birthYM,
                    targetYM = Smiths.richardTargetCollectSSYM,
                    baseAmount = Smiths.richardBaseSSBenefit,
                )
            )
        )
    }
}