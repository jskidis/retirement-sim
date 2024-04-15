package config.sample

import Amount
import RecIdentifier
import YearMonth
import asset.AssetProgression
import asset.AssetType
import asset.SimpleAssetGainCreator
import cashflow.IRAContribution
import cashflow.RmdCashFlowEventHandler
import config.AssetAttributeMap
import config.EmploymentConfig
import config.Person
import config.PersonConfigBuilder
import expense.AgeBasedExpenseAdjuster
import expense.BasicExpenseProgression
import expense.ExpenseProgression
import income.EmploymentIncomeProgression
import income.IncomeProgression
import inflation.StdInflationAmountAdjuster
import medical.*
import socsec.PrimarySSBenefitProgression
import socsec.SSBenefitProgression
import socsec.SecondarySSBenefitProgression
import socsec.SpousalSSBenefitProgression
import tax.*
import util.DateRange
import util.YearBasedConfig
import util.YearConfigPair

object Richard : PersonConfigBuilder {
    val incomeStart: Amount = 30000.0
    val expenseStart: Amount = 30000.0
    val employmentDates: DateRange = DateRange(end = YearMonth(2032, 1))
    val targetSSDate: YearMonth = YearMonth(year = 2035, 1)
    val baseSSBenefit: Amount = 27000.0

    val iraAcct = RecIdentifier(name = "Richard-IRA", person = Smiths.richard.name)
    val iraAcctBal: Amount = 500000.0

    val richardEmpConfig = EmploymentConfig(
        ident = RecIdentifier(name = "PartTime", person = Smiths.richard.name),
        startSalary = incomeStart,
        dateRange = employmentDates
    )

    override fun incomes(person: Person)
        : List<IncomeProgression> {
        val employmentConfigs = listOf(richardEmpConfig)
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
        val richIRA = AssetProgression(
            ident = iraAcct,
            assetType = AssetType.IRA,
            startBalance = iraAcctBal,
            cashflowEvents = listOf(
                RmdCashFlowEventHandler(person, NonWageTaxableProfile()),
                IRAContribution(
                    person = person,
                    contribName = "IRAContrib",
                    pctOfCap = 0.5,
                    taxabilityProfile = FedAndStateDeductProfile(),
                    includeCatchup = true
                )
            ),
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

    override fun benefits(person: Person): List<SSBenefitProgression> {
        return listOf(
            PrimarySSBenefitProgression(
                person = person,
                taxabilityProfile = SSBenefitTaxableProfile(),
                targetYM = targetSSDate,
                baseAmount = baseSSBenefit,
            )
        )
    }

    override fun secondaryBenefits(person: Person): List<SecondarySSBenefitProgression> {
        return listOf(
            SpousalSSBenefitProgression(
                person = person,
                spouse = Smiths.jane,
                taxabilityProfile = SSBenefitTaxableProfile()
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
                employments = listOf(Jane.janeEmpConfig),
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