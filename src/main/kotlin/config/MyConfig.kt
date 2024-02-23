package config

import AssetType
import Rate
import YearMonth
import asset.*
import expense.*
import income.BasicIncomeProgression
import income.IncomeConfig
import income.IncomeConfigProgression
import inflation.FixedRateInflationProgression
import inflation.StdInflationAmountAdjuster
import tax.*

val portfolioMap = LazyPortfolioLoader.loadPortfolios()
fun assetComp(name: String, pct: Rate = 1.0): AssetComposition =
    AssetComposition(name = name, pct = pct, rorProvider = portfolioMap[name]!!)

fun buildMyConfig(): SimConfig {

    val startYear = 2024
    val jason = Parent(name = "Jason", birthYM = YearMonth(1970, 1))
    val connie = Parent("Connie", YearMonth(1963, 4))
    val sydney = Dependant("Sydney", YearMonth(2001, 3))
    val zoe = Dependant("Zoe", YearMonth(2004, 9))

    val moneyMarketBal = 225000.0
    val stifelNRABal = 280000.0

    val householdMembers = HouseholdMembers(
        parent1 = jason, parent2 = connie,
        dependants = listOf(sydney, zoe)
    )

    val jasonIncomeConfig = IncomeConfig(
        name = "Ocelot", person = jason.name,
        taxabilityProfile = WageTaxableProfile()
    )
    jason.otherIncomes = listOf(
        IncomeConfigProgression(
            config = jasonIncomeConfig,
            progression = BasicIncomeProgression(
                startAmount = 180000.0,
                config = jasonIncomeConfig,
                adjusters = listOf(StdInflationAmountAdjuster())
            )
        )
    )

    val jasonExpenseConfig = ExpenseConfig(
        name = "Expenses", person = jason.name,
        taxabilityProfile = NonTaxableProfile()
    )
    jason.expenses = listOf(
        ExpenseConfigProgression(
            config = jasonExpenseConfig,
            progression = BasicExpenseProgression(
                startAmount = 35000.0,
                config = jasonExpenseConfig,
                adjusters = listOf(
                    StdInflationAmountAdjuster(),
                    AgeBasedExpenseAdjuster(jason.birthYM)
                )
            )
        )
    )

    val connieIncomeConfig = IncomeConfig(
        name = "Sumplicity", person = connie.name,
        taxabilityProfile = WageTaxableProfile()
    )
    connie.otherIncomes = listOf(
        IncomeConfigProgression(
            config = connieIncomeConfig,
            progression = BasicIncomeProgression(
                startAmount = 13500.0,
                config = connieIncomeConfig,
                adjusters = listOf(StdInflationAmountAdjuster())
            )
        )
    )

    val connieExpenseConfig = ExpenseConfig(
        name = "Expenses", person = connie.name,
        taxabilityProfile = NonTaxableProfile()
    )
    connie.expenses = listOf(
        ExpenseConfigProgression(
            config = connieExpenseConfig,
            progression = BasicExpenseProgression(
                startAmount = 35000.0,
                config = connieExpenseConfig,
                adjusters = listOf(
                    StdInflationAmountAdjuster(),
                    AgeBasedExpenseAdjuster(jason.birthYM)
                )
            )
        )
    )

    val zoeExpenseConfig = ExpenseConfig(
        name = "Expenses", person = zoe.name,
        taxabilityProfile = NonTaxableProfile()
    )
    zoe.contribExpenses = listOf(
        ExpenseConfigProgression(
            config = zoeExpenseConfig,
            progression = SCurveDecreasingExpense(
                startAmount = 20000.0,
                startDecYear = startYear,
                numYears = 10,
                config = zoeExpenseConfig,
                adjusters = listOf(StdInflationAmountAdjuster())
            )
        )
    )

    val householdExpensesConfig = ExpenseConfig(
        name = "Expenses", person = "Household",
        taxabilityProfile = NonTaxableProfile()
    )

    val householdExpenseProgression = ExpenseConfigProgression(
        config = householdExpensesConfig,
        progression = BasicExpenseProgression(
            startAmount = 40000.0,
            config = householdExpensesConfig,
            adjusters = listOf(StdInflationAmountAdjuster())
        )
    )

    val trustMoneyMarketConfig = AssetConfig(
        name = "MoneyMarket",
        person = "Trust",
        taxabilityProfile = NonWageTaxableProfile(),
        AssetType.CASH,
        minMaxProvider = NoMinMaxBalProvider(),
        assetCompMap = listOf(
            YearlyAssetComposition(2023, listOf(assetComp("US Cash")))
        )
    )

    val trustMoneyMarketProgression = AssetConfigProgression(
        config = trustMoneyMarketConfig,
        progression = BasicAssetProgression(moneyMarketBal, trustMoneyMarketConfig)
    )

    val trustStifelAssetConfig = AssetConfig(
        name = "Stifel-NRA",
        person = "Trust",
        taxabilityProfile = NonRetirementAssetTaxableProfile(),
        AssetType.INVEST,
        minMaxProvider = NoMinMaxBalProvider(),
        assetCompMap = listOf(
            YearlyAssetComposition(2023, listOf(assetComp("US Stocks")))
        )
    )

    val trustStifelProgression = AssetConfigProgression(
        config = trustStifelAssetConfig,
        progression = BasicAssetProgression(stifelNRABal, trustStifelAssetConfig)
    )


    val taxCalcConfig = TaxCalcConfig(
        fed = CurrentFedTaxJointBrackets,
        state = CurrentStateTaxJointBrackets,
        socSec = EmployeeSocSecTaxCalc(),
        medicare = EmployeeMedicareTaxCalc(),
    )

    return SimConfig(
        startYear = startYear,
        householdMembers = householdMembers,
        inflationConfig = FixedRateInflationProgression(0.03),
        householdExpenses = listOf(householdExpenseProgression),
        jointAssets = listOf(trustStifelProgression, trustMoneyMarketProgression),
        taxConfig = taxCalcConfig,
    )
}
