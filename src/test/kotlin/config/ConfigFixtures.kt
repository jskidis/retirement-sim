package config

import Amount
import Name
import RecIdentifier
import Year
import YearMonth
import asset.AssetProgression
import asset.assetProgressionFixture
import expense.ExpenseProgression
import expense.ExpenseRec
import expense.expenseProgressionFixture
import income.BonusCalculator
import income.IncomeProgression
import income.incomeProgressionFixture
import inflation.InflationRec
import inflationConfigFixture
import medical.MedInsuranceProgression
import netspend.NetSpendAllocationConfig
import progression.Progression
import socsec.SSBenefitProgression
import socsec.SecondarySSBenefitProgression
import socsec.benefitsProgressionFixture
import socsec.secondaryProgressionFixture
import tax.ITaxesProcessor
import tax.TaxCalcConfig
import tax.TaxesProcessorFixture
import tax.taxConfigFixture
import util.DateRange
import util.YearBasedConfig
import util.currentDate

fun configFixture(
    startYear: Year = currentDate.year + 1,
    householdConfig: HouseholdConfig = householdConfigFixture(householdMembersFixture()),
    inflationConfig: Progression<InflationRec> = inflationConfigFixture(),
    taxConfig: YearBasedConfig<TaxCalcConfig> = taxConfigFixture(),
    assetOrdering: NetSpendAllocationConfig = NetSpendAllocationConfig(listOf(), listOf()),
    rothConversion: RothConversionConfig? = null,
    taxesProcessor: ITaxesProcessor = TaxesProcessorFixture()

) =
    SimConfig(
        startYear = startYear,
        household = householdConfig,
        inflationConfig = inflationConfig,
        taxConfig = taxConfig,
        assetOrdering = assetOrdering,
        rothConversion = rothConversion,
        taxesProcessor = taxesProcessor
    )

fun personFixture(
    name: Name = "Person",
    birthYM: YearMonth = YearMonth(year = 1980, month = 0),
    actuarialGender: ActuarialGender = ActuarialGender.FEMALE,
) = Person(name, birthYM, actuarialGender)

fun householdConfigFixture(
    householdMembers: HouseholdMembers = householdMembersFixture(),
    expenses: List<ExpenseProgression> = ArrayList(),
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
    incomeConfigs: List<IncomeProgression> = listOf(
        incomeProgressionFixture("Income", name)),
    expenseConfigs: List<Progression<ExpenseRec>> = listOf(
        expenseProgressionFixture("Expense", name)),
    assetConfigs: List<AssetProgression> = listOf(
        assetProgressionFixture("Asset", name)),
    benefitConfigs: List<SSBenefitProgression> = listOf(
        benefitsProgressionFixture()),
    secondaryBenefitConfigs: List<SecondarySSBenefitProgression> = listOf(
        secondaryProgressionFixture()),
    medInsuranceConfigs: List<MedInsuranceProgression> = listOf()
) =
    ParentConfig(
        personFixture(name),
        incomeConfigs,
        expenseConfigs,
        assetConfigs,
        benefitConfigs,
        secondaryBenefitConfigs,
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
        ident = RecIdentifier(name = name, person = person),
        dateRange = dateRange,
        startSalary = startSalary,
        bonusCalc = bonusCalc,
        employerInsurance = employerInsurance
    )


