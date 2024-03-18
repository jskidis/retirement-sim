package config

import Name
import Year
import YearMonth
import asset.AssetConfigProgression
import asset.NetSpendAllocationConfig
import asset.assetConfigProgressFixture
import expense.ExpenseConfigProgression
import expense.expenseCfgProgessFixture
import income.IncomeConfigProgression
import income.incomeCfgProgessFixture
import inflation.InflationRec
import inflationConfigFixture
import progression.Progression
import tax.TaxCalcConfig
import tax.taxConfigFixture

fun configFixture(
    startYear: Year = 2020,
    householdConfig: HouseholdConfig = householdConfigFixture(householdMembersFixture()),
    inflationConfig: Progression<InflationRec> = inflationConfigFixture(),
    taxConfig: TaxCalcConfig = taxConfigFixture(),
    assetOrdering: NetSpendAllocationConfig = assetOrderingFixture(householdConfig)) =
    SimConfig(
        startYear = startYear,
        household = householdConfig,
        inflationConfig = inflationConfig,
        taxConfig = taxConfig,
        assetOrdering = assetOrdering
    )

fun personFixture(
    name: Name,
    birthYM: YearMonth = YearMonth(year = 1980, month = 0),
    actuarialGender: ActuarialGender = ActuarialGender.FEMALE,
) = Person(name, birthYM, actuarialGender)

fun householdConfigFixture(
    householdMembers: HouseholdMembers = householdMembersFixture(),
    expenses: List<ExpenseConfigProgression> = ArrayList(),
    jointAssets: List<AssetConfigProgression> = ArrayList(),
) = HouseholdConfig(householdMembers, expenses, jointAssets)

fun assetOrderingFixture(
    householdConfig: HouseholdConfig
) = NetSpendAllocationConfig(householdConfig.jointAssets, householdConfig.jointAssets)

fun householdMembersByNameFixture(
    parent1: Name = "Parent 1",
    parent2: Name = "Parent 2",
    dependants: List<Name> = listOf(),
) = householdMembersFixture(
    parent1Config = parentConfigFixture(parent1),
    parent2Config = parentConfigFixture(parent2),
    dependantsConfig = dependants.map { dependantConfigFixture(it) }
)

fun householdMembersFixture(
    parent1Config: ParentConfig = parentConfigFixture("Parent1"),
    parent2Config: ParentConfig = parentConfigFixture("Parent2"),
    dependantsConfig: List<DependantConfig> = listOf()
) = HouseholdMembers(
    parent1 = parent1Config,
    parent2 = parent2Config,
    dependants = dependantsConfig
)

fun parentConfigFixture(name: Name,
    incomeConfigs: List<IncomeConfigProgression> = listOf(incomeCfgProgessFixture("Income", name)),
    expenseConfigs: List<ExpenseConfigProgression> = listOf(expenseCfgProgessFixture("Expense", name)),
    assetConfigs: List<AssetConfigProgression> = listOf(assetConfigProgressFixture("Asset", name))
) = ParentConfig(personFixture(name), incomeConfigs,expenseConfigs, assetConfigs)

fun dependantConfigFixture(name: Name,
    incomeConfigs: List<IncomeConfigProgression> = listOf(incomeCfgProgessFixture("Income", name)),
    expenseConfigs: List<ExpenseConfigProgression> = listOf(expenseCfgProgessFixture("Expense", name)),
    assetConfigs: List<AssetConfigProgression> = listOf(assetConfigProgressFixture("Asset", name))
) = DependantConfig(personFixture(name), incomeConfigs,expenseConfigs, assetConfigs)



