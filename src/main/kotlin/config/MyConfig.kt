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
    val moneyMarketBal = 225000.0
    val stifelNRABal = 280000.0


    val jason = Person(
        name = "Jason",
        birthYM = YearMonth(1970, 1),
        actuarialGender = ActuarialGender.MALE
    )
    val connie = Person(
        name = "Connie",
        birthYM = YearMonth(1963, 4),
        actuarialGender = ActuarialGender.FEMALE
    )
    val sydney = Person(
        name = "Sydney",
        birthYM = YearMonth(2001, 3),
        actuarialGender = ActuarialGender.FEMALE
    )
    val zoe = Person(
        name = "Zoe",
        birthYM = YearMonth(2004, 9),
        actuarialGender = ActuarialGender.FEMALE
    )

    val jasonIncomeConfig = IncomeConfig(
        name = "Ocelot", person = jason.name,
        taxabilityProfile = WageTaxableProfile()
    )
    val jasonIncomes = listOf(
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
    val jasonExpenses = listOf(
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

    val jasonConfig = ParentConfig(
        person = jason,
        incomes = jasonIncomes,
        expenses = jasonExpenses
    )

    val connieIncomeConfig = IncomeConfig(
        name = "Sumplicity", person = connie.name,
        taxabilityProfile = WageTaxableProfile()
    )
    val connieIncomes = listOf(
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
    val connieExpenses = listOf(
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

    val connieConfig = ParentConfig(
        person = connie,
        incomes = connieIncomes,
        expenses = connieExpenses
    )

    val zoeExpenseConfig = ExpenseConfig(
        name = "Expenses", person = zoe.name,
        taxabilityProfile = NonTaxableProfile()
    )
    val zoeContributingExpenses = listOf(
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

    val zoeConfig = DependantConfig(person = zoe, expenses = zoeContributingExpenses)
    val sydneyConfig = DependantConfig(person = sydney)

    val householdExpensesConfig = ExpenseConfig(
        name = "Expenses", person = "Household",
        taxabilityProfile = NonTaxableProfile()
    )
    val householdExpenses = ExpenseConfigProgression(
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
    val trustMoneyMarket = AssetConfigProgression(
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
    val trustStifelAsset = AssetConfigProgression(
        config = trustStifelAssetConfig,
        progression = BasicAssetProgression(stifelNRABal, trustStifelAssetConfig)
    )


    val taxCalcConfig = TaxCalcConfig(
        fed = CurrentFedTaxJointBrackets,
        state = CurrentStateTaxJointBrackets,
        socSec = EmployeeSocSecTaxCalc(),
        medicare = EmployeeMedicareTaxCalc(),
    )

    val householdMembers = HouseholdMembers(
        parent1 = jasonConfig, parent2 = connieConfig,
        dependants = listOf(sydneyConfig, zoeConfig)
    )
    val householdConfig = HouseholdConfig(
        members = householdMembers,
        expenses = listOf(element = householdExpenses),
        jointAssets = listOf(trustMoneyMarket, trustStifelAsset)
    )


    return SimConfig(
        startYear = startYear,
        household = householdConfig,
        inflationConfig = FixedRateInflationProgression(0.03),
        taxConfig = taxCalcConfig,
    )
}
