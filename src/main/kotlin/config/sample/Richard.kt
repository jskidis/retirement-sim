package config.sample

import Amount
import RecIdentifier
import YearMonth
import asset.AssetProgression
import asset.RmdRequiredDistHandler
import asset.SimpleAssetGainCreator
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
import medical.*
import socsec.FixedDateAmountSSBenefitProgression
import socsec.SSBenefitConfig
import socsec.SSBenefitConfigProgression
import tax.NonTaxableProfile
import tax.SSBenefitTaxableProfile
import util.DateRange
import util.YearBasedConfig
import util.YearConfigPair

object Richard : ParentConfigBuilder {
    val incomeStart: Amount = 30000.0
    val expenseStart: Amount = 30000.0
    val employmentDates: DateRange = DateRange(end = YearMonth(2032, 1))
    val targetSSDate: YearMonth = YearMonth(year = 2035, 1)
    val baseSSBenefit: Amount = 27000.0

    val iraAcct = RecIdentifier(name = "Richard-IRA", person = Smiths.richard.name)
    val iraAcctBal: Amount = 500000.0


    override fun employmentConfigs(person: Person): List<EmploymentConfig> = listOf(
        EmploymentConfig(
            name = "PartTime", person = person.name,
            startSalary = incomeStart,
            dateRange = employmentDates
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
                    startAmount = expenseStart,
                    config = expenseConfig,
                    adjusters = listOf(
                        StdInflationAmountAdjuster(),
                        AgeBasedExpenseAdjuster(person.birthYM)
                    )
                )
            )
        )
    }

    override fun assets(person: Person): List<AssetProgression> {
        val richIRA = AssetProgression(
            ident = iraAcct,
            startBalance = iraAcctBal,
            requiredDistHandler = RmdRequiredDistHandler(person),
            gainCreator = SimpleAssetGainCreator(
                taxability = NonTaxableProfile(),
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

        return listOf(richIRA)
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

    override fun medInsurance(person: Person): List<MedInsuranceProgression> {
        return listOf(
            MedicareProgression(
                birthYM = person.birthYM,
                parts = listOf(
                    MedicarePartType.PARTB,
                    MedicarePartType.PARTD,
                    MedicarePartType.DENTAL,
                )),
            EmployerInsPremProgression(
                employments = Jane.employmentConfigs(Smiths.jane),
                relation = RelationToInsured.SPOUSE
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