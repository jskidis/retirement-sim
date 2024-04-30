package config

import Amount
import Name
import RecIdentifier
import Year
import YearMonth
import asset.AssetProgression
import asset.assetProgressionFixture
import cashflow.CashFlowEventConfig
import departed.DepartureConfig
import departed.NeverDepartConfig
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
import tax.TaxCalcConfig
import tax.TaxProcessorConfig
import tax.TaxesProcessorFixture
import tax.taxConfigFixture
import transfers.TransferGenerator
import util.DateRange
import util.YearBasedConfig
import util.currentDate

fun configFixture(
    startYear: Year = currentDate.year + 1,
    householdConfig: HouseholdConfig = householdConfigFixture(),
    inflationConfig: Progression<InflationRec> = inflationConfigFixture(),
    taxConfig: YearBasedConfig<TaxCalcConfig> = taxConfigFixture(),
    assetOrdering: NetSpendAllocationConfig = NetSpendAllocationConfig(listOf(), listOf()),
    transferGenerators: List<TransferGenerator> = listOf(),
    taxesProcessor: TaxProcessorConfig = TaxesProcessorFixture()

) =
    SimConfig(
        startYear = startYear,
        household = householdConfig,
        inflationConfig = inflationConfig,
        taxCalcConfig = taxConfig,
        assetOrdering = assetOrdering,
        transferGenerators = transferGenerators,
        taxesProcessor = taxesProcessor
    )

fun personFixture(
    name: Name = "Person",
    birthYM: YearMonth = YearMonth(year = 1980, month = 0),
    actuarialGender: ActuarialGender = ActuarialGender.FEMALE,
    isPrimary: Boolean = false
) = Person(name, birthYM, actuarialGender, isPrimary)

fun householdConfigFixture(
    householdMembers: List<PersonConfig> = listOf(personConfigFixture()),
    expenses: List<ExpenseProgression> = ArrayList(),
    jointAssets: List<AssetProgression> = ArrayList(),
) = HouseholdConfig(householdMembers, expenses, jointAssets)

/*
fun assetOrderingFixture(
    householdConfig: HouseholdConfig,
) = NetSpendAllocationConfig(householdConfig.jointAssets, householdConfig.jointAssets)

*/

fun personConfigFixture(
    person: Person = personFixture("Person"),
    departureConfig: DepartureConfig = NeverDepartConfig(),
    incomeConfigs: List<IncomeProgression> = listOf(
        incomeProgressionFixture("Income", person.name)),
    expenseConfigs: List<Progression<ExpenseRec>> = listOf(
        expenseProgressionFixture("Expense", person.name)),
    assetConfigs: List<AssetProgression> = listOf(
        assetProgressionFixture("Asset", person.name)),
    benefitConfigs: List<SSBenefitProgression> = listOf(
        benefitsProgressionFixture()),
    secondaryBenefitConfigs: List<SecondarySSBenefitProgression> = listOf(
        secondaryProgressionFixture()),
    medInsuranceConfigs: List<MedInsuranceProgression> = listOf(),
    cashFlowEvents: List<CashFlowEventConfig> = listOf()
) =
    PersonConfig(
        person,
        departureConfig,
        incomeConfigs,
        expenseConfigs,
        assetConfigs,
        benefitConfigs,
        secondaryBenefitConfigs,
        medInsuranceConfigs,
        cashFlowEvents)

fun employmentConfigFixture(
    name: Name = "Employment",
    person: Name = "Person",
    dateRange: DateRange = DateRange(),
    startSalary: Amount = 0.0,
    bonusCalc: BonusCalculator? = null,
    employerInsurance: EmployerInsurance? = EmployerInsurance(
        selfCost = 0.0, spouseCost = 0.0, dependentCost = 0.0,
        cobraConfig = CobraConfig(0.0, 0.0, 0.0)
    ),
): EmploymentConfig =
    EmploymentConfig(
        ident = RecIdentifier(name = name, person = person),
        dateRange = dateRange,
        startSalary = startSalary,
        bonusCalc = bonusCalc,
        employerInsurance = employerInsurance
    )


