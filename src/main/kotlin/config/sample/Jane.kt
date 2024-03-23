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
import income.EmploymentIncomeProgression
import income.IncomeConfigProgression
import inflation.StdInflationAmountAdjuster
import socsec.FixedDateAmountSSBenefitProgression
import socsec.SSBenefitConfig
import socsec.SSBenefitConfigProgression
import tax.NonTaxableProfile
import tax.SSBenefitTaxableProfile
import util.YearBasedConfig
import util.YearConfigPair

object Jane : ParentConfigBuilder {
    fun employmentConfigs(person: Person): List<EmploymentConfig> = listOf(
        EmploymentConfig(
            name = "Accenture", person = person.name,
            startSalary = Smiths.janeIncStart,
            dateRange = Smiths.janeEmploymentDate,
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

    override fun assets(person: Person): List<AssetConfigProgression> {
        val janeIRAConfig = AssetConfig(
            name = Smiths.janeIRAAcctName,
            person = person.name,
            taxabilityProfile = NonTaxableProfile(),
        )
        val janeIRAAsset = AssetConfigProgression(
            config = janeIRAConfig,
            spendAllocHandler = IRASpendAlloc(person),
            progression = AssetProgression(
                startBalance = Smiths.janeIRAAcctBal,
                config = janeIRAConfig,
                gainCreator = SimpleAssetGainCreator(),
                requiredDistHandler = RmdRequiredDistHandler(person),
                attributesSet = YearBasedConfig(
                    listOf(
                        YearConfigPair(
                            startYear = Smiths.startYear - 1,
                            config = AssetAttributeMap.assetComp("US Stocks")
                        ),
                        YearConfigPair(
                            startYear = Smiths.janeEmploymentDate.end.year,
                            config = AssetAttributeMap.assetComp("Stocks/Bonds 60/40")
                        )
                    ))
            )
        )

        return listOf(janeIRAAsset)
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
                    targetYM = Smiths.janeTargetCollectSSYM,
                    baseAmount = Smiths.janeBaseSSBenefit,
                )
            )
        )
    }
}