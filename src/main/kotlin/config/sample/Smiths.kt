package config.sample

import Year
import YearMonth
import config.*
import inflation.FixedRateInflationProgression
import netspend.*
import tax.NonWageTaxableProfile
import tax.currTaxConfig
import tax.rollbackTaxConfig
import util.YearBasedConfig
import util.YearConfigPair
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

        val taxCalcConfig = YearBasedConfig(listOf(
            YearConfigPair(startYear = 2024, config = currTaxConfig),
            YearConfigPair(startYear = 2027, config = rollbackTaxConfig),
        ))

        val savingsAllocConfig = NetSpendAssetConfig(
            ident = Household.savingsAcct,
            spendAllocHandler = CapReserveSpendAlloc(
                margin = .05,
                yearlyTargetMult = YearBasedConfig(
                    listOf(
                        YearConfigPair(2024, 2.0),
                        YearConfigPair(Jane.employmentDates.end.year, 3.0),
                        YearConfigPair(Jane.targetSSDate.year, 4.0)
                    )
                )),
        )
        val investAllocConfig = NetSpendAssetConfig(
            ident = Household.investAcct,
            spendAllocHandler = TaxableInvestSpendAllocHandler(minAcctBal = 1000.0),
        )
        val janeIraAllocConfig = NetSpendAssetConfig(
            ident = Jane.iraAcct,
            spendAllocHandler = IRASpendAlloc(jane, NonWageTaxableProfile())
        )
        val jane401kAllocConfig = NetSpendAssetConfig(
            ident = Jane.four01kAcct,
            spendAllocHandler = IRASpendAlloc(jane, NonWageTaxableProfile())
        )
        val richardIraAllocConfig = NetSpendAssetConfig(
            ident = Richard.iraAcct,
            spendAllocHandler = IRASpendAlloc(jane, NonWageTaxableProfile())
        )

        val withdrawOrder = listOf(
            savingsAllocConfig, investAllocConfig, jane401kAllocConfig,
            richardIraAllocConfig, janeIraAllocConfig
        )
        val depositOrder = listOf(savingsAllocConfig, investAllocConfig)
        val assetOrdering = NetSpendAllocationConfig(withdrawOrder, depositOrder)

        return SimConfig(
            startYear = startYear,
            household = householdConfig,
            inflationConfig = inflationConfig,
            taxConfig = taxCalcConfig,
            assetOrdering = assetOrdering
        )
    }
}