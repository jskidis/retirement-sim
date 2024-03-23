package config.sample

import asset.*
import config.AssetAttributeMap
import config.EmploymentConfig
import config.ParentConfigBuilder
import config.Person
import expense.AgeBasedExpenseAdjuster
import expense.BasicExpenseProgression
import expense.ExpenseConfig
import expense.ExpenseConfigProgression
import income.BasicIncomeProgression
import income.EmploymentIncomeProgression
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
import util.YearBasedConfig
import util.YearConfigPair

object Richard : ParentConfigBuilder {
    fun employmentConfigs(person: Person): List<EmploymentConfig> = listOf(
        EmploymentConfig(
            name = "PartTime", person = person.name,
            startSalary = Smiths.richardIncStart,
            dateRange = Smiths.richardEmploymentDate,
        )
    )

    override fun incomes(person: Person)
        : List<IncomeConfigProgression> {
        val employmentConfigs = employmentConfigs(person)
        return employmentConfigs.map {
            val incomeConfig = EmploymentConfig.incomeConfig(it)
            val progression = EmploymentIncomeProgression(it, listOf(StdInflationAmountAdjuster()))
            IncomeConfigProgression(incomeConfig, progression)
        }
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

    override fun assets(person: Person): List<AssetConfigProgression> {
        val richardIRAConfig = AssetConfig(
            name = Smiths.richardIRAAcctName,
            person = person.name,
            taxabilityProfile = NonTaxableProfile(),
        )
        val richIRAAsset = AssetConfigProgression(
            config = richardIRAConfig,
            spendAllocHandler = IRASpendAlloc(person),
            progression = AssetProgression(
                startBalance = Smiths.richardIRAAcctBal,
                config = richardIRAConfig,
                gainCreator = SimpleAssetGainCreator(),
                requiredDistHandler = RmdRequiredDistHandler(person),
                attributesSet = YearBasedConfig(
                    listOf(
                        YearConfigPair(
                            startYear = Smiths.startYear - 1,
                            config = AssetAttributeMap.assetComp("US Stocks")
                        ),
                        YearConfigPair(
                            startYear = Smiths.richardEmploymentDate.end.year,
                            config = AssetAttributeMap.assetComp("Stocks/Bonds 60/40")
                        )
                    ))
            )
        )

        return listOf(richIRAAsset)
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