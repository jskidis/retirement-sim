package config.sample

import Amount
import RecIdentifier
import YearMonth
import asset.AssetProgression
import asset.RmdCashFlowEventHandler
import asset.SimpleAssetGainCreator
import config.*
import expense.AgeBasedExpenseAdjuster
import expense.BasicExpenseProgression
import expense.ExpenseProgression
import income.EmploymentIncomeProgression
import income.IncomeProgression
import inflation.StdInflationAmountAdjuster
import medical.*
import socsec.FixedDateAmountSSBenefitProgression
import socsec.SSBenefitProgression
import tax.NonDeductProfile
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

    val iraAcct = RecIdentifier(name = "Jane-IRA", person = Smiths.jane.name)
    val iraAcctBal: Amount = 1000000.0

    override fun employmentConfigs(person: Person): List<EmploymentConfig> = listOf(
        EmploymentConfig(
            ident = RecIdentifier(name = "Accenture", person = person.name),
            startSalary = incomeStart,
            dateRange = employmentDates,
            employerInsurance = EmployerInsurance(
                selfCost = 2500.0,
                spouseCost = 3000.0,
                dependantCost = 1000.0
            )
        )
    )

    override fun incomes(person: Person)
        : List<IncomeProgression> {
        val employmentConfigs = employmentConfigs(person)
        return employmentConfigs.map {
            EmploymentIncomeProgression(it, listOf(StdInflationAmountAdjuster()))
        }
    }

    override fun expenses(person: Person): List<ExpenseProgression> {
        return listOf(
            BasicExpenseProgression(
                ident = RecIdentifier(name = "Expenses", person = person.name),
                startAmount = expenseStart,
                taxabilityProfile = NonDeductProfile(),
                adjusters = listOf(
                    StdInflationAmountAdjuster(),
                    AgeBasedExpenseAdjuster(person.birthYM)
                )
            )
        )
    }

    override fun assets(person: Person): List<AssetProgression> {
        val janeIRA = AssetProgression(
            ident = iraAcct,
            startBalance = iraAcctBal,
            cashflowEvents = listOf(RmdCashFlowEventHandler(person)),
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
        return listOf(janeIRA)
    }

    override fun benefits(person: Person): List<SSBenefitProgression> {
        return listOf(
            FixedDateAmountSSBenefitProgression(
                ident = RecIdentifier(name = "Primary", person = person.name),
                birthYM = person.birthYM,
                targetYM = targetSSDate,
                baseAmount = baseSSBenefit,
                taxabilityProfile = SSBenefitTaxableProfile(),
            )
        )
    }

    override fun medInsurance(person: Person): List<MedInsuranceProgression> {
        return listOf(
            EmployerInsPremProgression(
                employments = employmentConfigs(person),
                relation = RelationToInsured.SELF
            ),
            MedicareProgression(
                birthYM = person.birthYM,
                parts = listOf(
                    MedicarePartType.PARTB,
                    MedicarePartType.PARTD,
                    MedicarePartType.DENTAL,
                )),
            MarketplacePremProgression(
                birthYM = person.birthYM,
                medalType = MPMedalType.SILVER,
                planType = MPPlanType.HMO,
                includeDental = true
            )
        )
    }
}