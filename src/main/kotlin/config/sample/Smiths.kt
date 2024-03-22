package config.sample

import Amount
import Year
import YearMonth
import asset.NetSpendAllocationConfig
import config.*
import inflation.FixedRateInflationProgression
import tax.*
import util.DateRange
import util.currentDate


class Smiths : ConfigBuilder {
    companion object {
        val startYear: Year = currentDate.year

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

        val savingsAcctName = "Savings"
        val savingsBal: Amount = 50000.0
        val investAcctName = "BigInvBank"
        val investBal = 200000.0

        val janeIRAAcctName: String = "Jane-IRA"
        val janeIRAAcctBal: Amount = 1000000.0

        val richardIRAAcctName: String = "Richard-IRA"
        val richardIRAAcctBal: Amount = 500000.0
    }

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

        val withdrawOrdering = listOf(
            householdConfig.jointAssets.find { it.config.name == savingsAcctName },
            householdConfig.jointAssets.find { it.config.name == investAcctName }
        ).mapNotNull { it }

        val depositOrdering = listOf(
            householdConfig.jointAssets.find { it.config.name == savingsAcctName },
            householdConfig.jointAssets.find { it.config.name == investAcctName }
        ).mapNotNull { it }

        val assetOrdering = NetSpendAllocationConfig(withdrawOrdering, depositOrdering)

        return SimConfig(
            startYear = startYear,
            household = householdConfig,
            inflationConfig = inflationConfig,
            taxConfig = taxCalcConfig,
            assetOrdering = assetOrdering
        )
    }
}