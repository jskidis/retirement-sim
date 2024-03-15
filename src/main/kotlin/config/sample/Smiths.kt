package config.sample

import Amount
import Year
import YearMonth
import config.*
import inflation.FixedRateInflationProgression
import tax.*
import util.DateRange


object Smiths : ConfigBuilder {
    val startYear: Year = 2024

    val janeIncStart: Amount = 135000.0
    val janeExpStart: Amount = 30000.0
    val janeEmploymentDate: DateRange = DateRange(end = YearMonth(2037, 6))
    val janeTargetCollectSSYM: YearMonth = YearMonth(year = 2042, 6)
    val janeBaseSSBenefit: Amount = 40500.0

    val richardIncStart: Amount = 30000.0
    val richardExpStart: Amount = 30000.0
    val richardEmploymentDate: DateRange = DateRange(end = YearMonth(2032, 1))
    val richardTargetCollectSSYM: YearMonth = YearMonth(year = 2035, 1)
    val richardBaseSSBenefit: Amount = 27000.0

    val suzyExpStart: Amount = 20000.0
    val jonnyExpStart: Amount = 20000.0

    val houseExpStart: Amount = 25000.0
    val savingsBal: Amount = 50000.0
    val investBal = 200000.0

    override fun buildConfig(): SimConfig {
        val jane = Person(
            name = "Jane",
            birthYM = YearMonth(1975, 4),
            actuarialGender = ActuarialGender.FEMALE
        )
        val richard = Person(
            name = "Richard",
            birthYM = YearMonth(1965, 1),
            actuarialGender = ActuarialGender.MALE
        )
        val suzy = Person(
            name = "Suzy",
            birthYM = YearMonth(2005, 3),
            actuarialGender = ActuarialGender.FEMALE
        )
        val jonny = Person(
            name = "Jonny",
            birthYM = YearMonth(2010, 9),
            actuarialGender = ActuarialGender.MALE
        )

        val householdMembers = HouseholdMembers(
            parent1 = Jane.buildConfig(jane),
            parent2 = Richard.buildConfig(richard),
            dependants = listOf(
                Jonny.buildConfig(suzy),
                Suzy.buildConfig(jonny))
        )
        val householdConfig = Household.buildConfig(householdMembers)

        val inflationConfig = FixedRateInflationProgression(0.03)

        val taxCalcConfig = TaxCalcConfig(
            fed = CurrentFedTaxBrackets,
            fedLTG = CurrentFedLTGCalc,
            state = CurrentStateTaxBrackets,
            socSec = EmployeeSocSecTaxCalc(),
            medicare = EmployeeMedicareTaxCalc(),
        )

        return SimConfig(
            startYear = startYear,
            household = householdConfig,
            inflationConfig = inflationConfig,
            taxConfig = taxCalcConfig,
        )
    }
}