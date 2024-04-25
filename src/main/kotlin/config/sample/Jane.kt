package config.sample

import Amount
import RecIdentifier
import YearMonth
import asset.AssetProgression
import asset.AssetType
import asset.SimpleAssetGainCreator
import cashflow.*
import config.*
import expense.AgeBasedExpenseAdjuster
import expense.BasicExpenseProgression
import income.EmploymentIncomeProgression
import income.IncomeProgression
import inflation.StdInflationAmountAdjuster
import medical.*
import socsec.FlexibleClaimDateProvider
import socsec.NewIncomeAdjustBaseAmountProvider
import socsec.PrimarySSBenefitProgression
import tax.*
import util.DateRange
import util.YearBasedConfig
import util.YearConfigPair

object Jane : PersonConfigBuilder {
    val incomeStart: Amount = 135000.0
    val expenseStart: Amount = 30000.0
    val employmentDates: DateRange = DateRange(end = YearMonth(2037, 6))
    val targetSSDate: YearMonth = YearMonth(year = 2042, 6)

    val baseSSBenefit: Amount = 40500.0
    val ssBenefitIncPer100k: Amount = 600.0

    val iraAcct = RecIdentifier(name = "Jane-IRA", person = Smiths.jane.name)
    val iraAcctBal: Amount = 500000.0

    val rothAcct = RecIdentifier(name = "Jane-Roth", person = Smiths.jane.name)
    val rothAcctBal: Amount = 100000.0

    val four01kAcct = RecIdentifier(name = "Jane-401k", person = Smiths.jane.name)
    val four01kAcctBal = 250000.0

    val janeEmpConfig = EmploymentConfig(
        ident = RecIdentifier(name = "BigCo", person = Smiths.jane.name),
        startSalary = incomeStart,
        dateRange = employmentDates,
        employerInsurance = EmployerInsurance(
            selfCost = 2500.0,
            spouseCost = 2500.0,
            dependentCost = 1000.0,
            cobraConfig = CobraConfig(
                selfCost = 2.04 * 2500.0,
                spouseCost = 2.04 * 2500.0,
                dependentCost = 2.04 * 1000.0
            )
        )
    )


    override fun incomes(person: Person): List<IncomeProgression> {
        val employmentConfigs = listOf(janeEmpConfig)
        return employmentConfigs.map {
            EmploymentIncomeProgression(it, listOf(StdInflationAmountAdjuster()))
        }
    }

    override fun expenses(person: Person) = listOf(
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

    override fun assets(person: Person): List<AssetProgression> {
        val janeIRA = AssetProgression(
            ident = iraAcct,
            assetType = AssetType.IRA,
            startBalance = iraAcctBal,
            gainCreator = SimpleAssetGainCreator(
                taxability = NonTaxableProfile(),
                attributesSet = YearBasedConfig(
                    listOf(
                        YearConfigPair(
                            startYear = Smiths.startYear,
                            config = AssetAttributeMap.assetComp("US Stocks")
                        ),
                        YearConfigPair(
                            startYear = employmentDates.end.year,
                            config = AssetAttributeMap.assetComp("Stocks/Bonds 60/40")
                        )
                    ))
            )
        )
        val janeRoth = AssetProgression(
            ident = rothAcct,
            assetType = AssetType.ROTH,
            startBalance = rothAcctBal,
            gainCreator = SimpleAssetGainCreator(
                taxability = NonTaxableProfile(),
                attributesSet = YearBasedConfig(
                    listOf(
                        YearConfigPair(
                            startYear = Smiths.startYear,
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
            assetType = AssetType.STD401K,
            startBalance = four01kAcctBal,
            gainCreator = SimpleAssetGainCreator(
                taxability = NonTaxableProfile(),
                attributesSet = YearBasedConfig(
                    listOf(
                        YearConfigPair(
                            startYear = Smiths.startYear,
                            config = AssetAttributeMap.assetComp("US Stocks")
                        ),
                        YearConfigPair(
                            startYear = employmentDates.end.year,
                            config = AssetAttributeMap.assetComp("Stocks/Bonds 60/40")
                        )
                    ))
            )
        )
        return listOf(janeIRA, janeRoth, jane401K)
    }

    override fun benefits(person: Person) = listOf(
        PrimarySSBenefitProgression(
            person = person,
            taxabilityProfile = SSBenefitTaxableProfile(),
            baseAmountProvider = NewIncomeAdjustBaseAmountProvider(
                baseSSBenefit, ssBenefitIncPer100k),
            claimDateProvider = FlexibleClaimDateProvider(
                person.birthYM, targetSSDate, 5.0),
        )
    )

    override fun medInsurance(person: Person) = listOf(
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

    override fun cashFlowEvents(person: Person) = listOf(
        CashFlowEventConfig(
            assetIdent = iraAcct,
            handler = RmdCashFlowEventHandler(person, NonWageTaxableProfile()),
        ),
        CashFlowEventConfig(
            assetIdent = four01kAcct,
            handler = RmdCashFlowEventHandler(person, NonWageTaxableProfile()),
        ),
        CashFlowEventConfig(
            assetIdent = four01kAcct,
            handler = EmployerRetirement(
                empConfig = janeEmpConfig, person = person,
                contributionName = "Jane-401K-Contrib",
                taxabilityProfile = FedAndStateDeductProfile(),
                amountRetriever = MaxPlusCatchupAmountRetriever()),
        ),
        CashFlowEventConfig(
            assetIdent = four01kAcct,
            handler = EmployerRetirement(
                empConfig = janeEmpConfig, person = person,
                contributionName = "Jane-401K-Match",
                taxabilityProfile = FedAndStateDeductProfile(),
                amountRetriever = EmployerMatchAmountRetriever(.03)),
        ))
}