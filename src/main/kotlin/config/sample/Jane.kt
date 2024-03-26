package config.sample

import Amount
import YearMonth
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
import util.DateRange
import util.YearBasedConfig
import util.YearConfigPair

object Jane : ParentConfigBuilder {
    val incomeStart: Amount = 135000.0
    val expenseStart: Amount = 30000.0
    val employmentDates: DateRange = DateRange(end = YearMonth(2037, 6))
    val targetSSDate: YearMonth = YearMonth(year = 2042, 6)
    val baseSSBenefit: Amount = 40500.0

    val iraAcctName: String = "Jane-IRA"
    val iraAcctBal: Amount = 1000000.0

    override fun employmentConfigs(person: Person): List<EmploymentConfig> = listOf(
        EmploymentConfig(
            name = "Accenture", person = person.name,
            startSalary = incomeStart,
            dateRange = employmentDates,
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
                    startAmount = expenseStart,
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
            name = iraAcctName,
            person = person.name,
            taxabilityProfile = NonTaxableProfile(),
        )
        val janeIRAAsset = AssetConfigProgression(
            config = janeIRAConfig,
            spendAllocHandler = IRASpendAlloc(person),
            progression = AssetProgression(
                startBalance = iraAcctBal,
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
                            startYear = employmentDates.end.year,
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
                    targetYM = targetSSDate,
                    baseAmount = baseSSBenefit,
                )
            )
        )
    }
}