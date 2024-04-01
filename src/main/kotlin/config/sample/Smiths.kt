package config.sample

import Year
import YearMonth
import asset.NetSpendAllocationConfig
import config.*
import inflation.FixedRateInflationProgression
import tax.*
import util.currentDate


class Smiths : ConfigBuilder {
    companion object {
        val startYear: Year = currentDate.year

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
    }

    override fun buildConfig(): SimConfig {
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
            fedLTG = CurrentFedLTGBrackets,
            state = CurrentStateTaxBrackets,
            socSec = EmployeeSocSecTaxCalc(),
            medicare = EmployeeMedicareTaxCalc(),
        )

        val withdrawOrdering = listOf(
            householdConfig.jointAssets.find { it.config.name == Household.savingsAcctName },
            householdConfig.jointAssets.find { it.config.name == Household.investAcctName }
        ).mapNotNull { it }

        val depositOrdering = listOf(
            householdConfig.jointAssets.find { it.config.name == Household.savingsAcctName },
            householdConfig.jointAssets.find { it.config.name == Household.investAcctName }
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