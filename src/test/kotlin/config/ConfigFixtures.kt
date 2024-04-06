package config

import Amount
import Name
import Year
import YearMonth
import asset.AssetProgression
import asset.NetSpendAllocationConfig
import asset.assetProgressionFixture
import expense.ExpenseConfigProgression
import expense.expenseCfgProgessFixture
import income.BonusCalculator
import income.IncomeConfigProgression
import income.incomeCfgProgessFixture
import inflation.InflationRec
import inflationConfigFixture
import medical.MedInsuranceProgression
import progression.Progression
import socsec.SSBenefitConfigProgression
import socsec.benefitsConfigProgressFixture
import tax.TaxCalcConfig
import tax.taxConfigFixture
import util.DateRange
import util.currentDate

fun configFixture(
    startYear: Year = currentDate.year + 1,
    householdConfig: HouseholdConfig = householdConfigFixture(householdMembersFixture()),
    inflationConfig: Progression<InflationRec> = inflationConfigFixture(),
    taxConfig: TaxCalcConfig = taxConfigFixture(),
    assetOrdering: NetSpendAllocationConfig = NetSpendAllocationConfig(listOf(), listOf()),
) =
    SimConfig(
        startYear = startYear,
        household = householdConfig,
        inflationConfig = inflationConfig,
        taxConfig = taxConfig,
        assetOrdering = assetOrdering
    )

fun personFixture(
    name: Name = "Person",
    birthYM: YearMonth = YearMonth(year = 1980, month = 0),
    actuarialGender: ActuarialGender = ActuarialGender.FEMALE,
) = Person(name, birthYM, actuarialGender)

fun householdConfigFixture(
    householdMembers: HouseholdMembers = householdMembersFixture(),
    expenses: List<ExpenseConfigProgression> = ArrayList(),
    jointAssets: List<AssetProgression> = ArrayList(),
) = HouseholdConfig(householdMembers, expenses, jointAssets)

/*
fun assetOrderingFixture(
    householdConfig: HouseholdConfig,
) = NetSpendAllocationConfig(householdConfig.jointAssets, householdConfig.jointAssets)

*/
fun householdMembersFixture(
    parent1Config: ParentConfig = parentConfigFixture("Parent1"),
    parent2Config: ParentConfig = parentConfigFixture("Parent2"),
    dependantsConfig: List<DependantConfig> = listOf(),
) = HouseholdMembers(
    parent1 = parent1Config,
    parent2 = parent2Config,
    dependants = dependantsConfig
)

fun parentConfigFixture(
    name: Name,
    incomeConfigs: List<IncomeConfigProgression> = listOf(
        incomeCfgProgessFixture("Income", name)),
    expenseConfigs: List<ExpenseConfigProgression> = listOf(
        expenseCfgProgessFixture("Expense", name)),
    assetConfigs: List<AssetProgression> = listOf(
        assetProgressionFixture("Asset", name)),
    benefitConfigs: List<SSBenefitConfigProgression> = listOf(
        benefitsConfigProgressFixture("SSBenefits")),
    medInsuranceConfigs: List<MedInsuranceProgression> = listOf()
) =
    ParentConfig(
        personFixture(name),
        incomeConfigs,
        expenseConfigs,
        assetConfigs,
        benefitConfigs,
        medInsuranceConfigs)

fun employmentConfigFixture(
    name: Name = "Employment",
    person: Name = "Person",
    dateRange: DateRange = DateRange(),
    startSalary: Amount = 0.0,
    bonusCalc: BonusCalculator? = null,
    employerInsurance: EmployerInsurance? = EmployerInsurance(0.0, 0.0, 0.0),
): EmploymentConfig =
    EmploymentConfig(
        name = name,
        person = person,
        dateRange = dateRange,
        startSalary = startSalary,
        bonusCalc = bonusCalc,
        employerInsurance = employerInsurance
    )


