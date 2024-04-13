package config.sample

import Amount
import RecIdentifier
import YearMonth
import asset.AssetProgression
import asset.SimpleAssetGainCreator
import cashflow.EmployerMatchAmountRetriever
import cashflow.EmployerRetirement
import cashflow.MaxPlusCatchupAmountRetriever
import cashflow.RmdCashFlowEventHandler
import config.*
import expense.AgeBasedExpenseAdjuster
import expense.BasicExpenseProgression
import expense.ExpenseProgression
import income.EmploymentIncomeProgression
import income.IncomeProgression
import inflation.StdInflationAmountAdjuster
import medical.*
import socsec.IncByIncomeFlexClaimSSBenefitProgression
import socsec.SSBenefitProgression
import tax.*
import util.DateRange
import util.YearBasedConfig
import util.YearConfigPair

object Jane : ParentConfigBuilder {
    val incomeStart: Amount = 135000.0
    val expenseStart: Amount = 30000.0
    val employmentDates: DateRange = DateRange(end = YearMonth(2037, 6))
    val targetSSDate: YearMonth = YearMonth(year = 2042, 6)

    val baseSSBenefit: Amount = 40500.0
    val ssBenefitIncPer100k: Amount = 600.0

    val iraAcct = RecIdentifier(name = "Jane-IRA", person = Smiths.jane.name)
    val iraAcctBal: Amount = 500000.0

    val four01kAcct = RecIdentifier(name = "Jane-401k", person = Smiths.jane.name)
    val four01kAcctBal = 250000.0

    val janeEmpConfig = EmploymentConfig(
        ident = RecIdentifier(name = "BigCo", person = Smiths.jane.name),
        startSalary = incomeStart,
        dateRange = employmentDates,
        employerInsurance = EmployerInsurance(
            selfCost = 2500.0,
            spouseCost = 3000.0,
            dependantCost = 1000.0
        )
    )


    override fun incomes(person: Person)
        : List<IncomeProgression> {
        val employmentConfigs = listOf(janeEmpConfig)
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
            cashflowEvents = listOf(RmdCashFlowEventHandler(person, NonWageTaxableProfile())),
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
        val jane401K = AssetProgression(
            ident = four01kAcct,
            startBalance = four01kAcctBal,
            cashflowEvents = listOf(
                RmdCashFlowEventHandler(person, NonWageTaxableProfile()),
                EmployerRetirement(
                    empConfig = janeEmpConfig,
                    person = person,
                    contributionName = "Jane-401K-Contrib",
                    taxabilityProfile = FedAndStateDeductProfile(),
                    amountRetriever = MaxPlusCatchupAmountRetriever()
                ),
                EmployerRetirement(
                    empConfig = janeEmpConfig,
                    person = person,
                    contributionName = "Jane-401K-Match",
                    taxabilityProfile = FedAndStateDeductProfile(),
                    amountRetriever = EmployerMatchAmountRetriever(.03)
                ),
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
        return listOf(janeIRA, jane401K)
    }

    override fun benefits(person: Person): List<SSBenefitProgression> {
        return listOf(
            IncByIncomeFlexClaimSSBenefitProgression(
                person = person,
                targetYM = targetSSDate,
                baseAmount = baseSSBenefit,
                incPer100k = ssBenefitIncPer100k,
                multipleOfExpense = 5.0,
                taxabilityProfile = SSBenefitTaxableProfile(),
            )
        )
    }

    override fun medInsurance(person: Person): List<MedInsuranceProgression> {
        return listOf(
            EmployerInsPremProgression(
                employments = listOf(janeEmpConfig),
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