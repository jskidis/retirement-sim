package config.sample

import Year
import YearMonth
import config.*
import inflation.StickyInflationProgression
import inflation.inflation40YearAvgs
import netspend.*
import tax.NonTaxableProfile
import tax.NonWageTaxableProfile
import tax.currTaxConfig
import tax.rollbackTaxConfig
import transfers.*
import util.YearBasedConfig
import util.YearConfigPair
import util.currentDate


class Smiths : ConfigBuilder {
    companion object {
        val startYear: Year = currentDate.year

        val jane = Person(
            name = "Jane",
            birthYM = YearMonth(1975, 4),
            actuarialGender = ActuarialGender.FEMALE,
            isPrimary = true
        )
        val richard = Person(
            name = "Richard",
            birthYM = YearMonth(1965, 1),
            actuarialGender = ActuarialGender.MALE,
            isPrimary = true
        )
        val suzy = Person(
            name = "Suzy",
            birthYM = YearMonth(2005, 3),
            actuarialGender = ActuarialGender.FEMALE,
            isPrimary = false
        )
        val jonny = Person(
            name = "Jonny",
            birthYM = YearMonth(2010, 9),
            actuarialGender = ActuarialGender.MALE,
            isPrimary = false
        )
    }

    override fun householdConfig(): HouseholdConfig {
        val householdMembers = listOf(
            Jane.buildConfig(jane),
            Richard.buildConfig(richard),
            Jonny.buildConfig(suzy),
            Suzy.buildConfig(jonny)
        )
        return Household.buildConfig(householdMembers)
    }

    override fun inflationConfig(): InflationConfig = StickyInflationProgression(
        initialStickiness = 0.9, meanAndSD = inflation40YearAvgs
    )

    override fun taxCalcConfig(): TaxCalcYearlyConfig =
        YearBasedConfig(
            listOf(
                YearConfigPair(startYear = 2024, config = currTaxConfig),
                YearConfigPair(startYear = 2026, config = rollbackTaxConfig),
            ))

    override fun assetOrdering(): NetSpendAllocationConfig {
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

        return NetSpendAllocationConfig(withdrawOrder, depositOrder)
    }

    override fun transferGenerators(): List<TransferGenerator> = listOf(
        RothConversionGenerator(
            amountCalc = YearBasedConfig(
                listOf(
                    YearConfigPair(startYear, RothIncomeLimitRothConv(MaxTaxRateRothConv(.24))),
                    YearConfigPair(2026, RothIncomeLimitRothConv(TilNextBracketRothConv())),
                    YearConfigPair(Jane.employmentDates.end.year + 1, MaxTaxRateRothConv(.20)),
                    YearConfigPair(2034, MaxTaxRateRothConv(.15)),
                    YearConfigPair(Jane.targetSSDate.year, MaxTaxRateRothConv(.25)),
                    YearConfigPair(jane.birthYM.year + 75, TilNextBracketRothConv())
                )),
            sourceDestPairs = listOf(
                Jane.iraAcct to Jane.rothAcct,
                Jane.four01kAcct to Jane.rothAcct,
            ),
            taxabilityProfile = NonWageTaxableProfile()
        ),
        CloseAccountsOnYear(
            year = Jane.employmentDates.end.year + 1,
            transferName = "Jane-401k-Transfer",
            taxabilityProfile = NonTaxableProfile(),
            sourceDestPairs = listOf(Jane.four01kAcct to Jane.iraAcct),
        ),
        CloseAccountsOnYear(
            year = 2050, transferName = "Jane-Death-IRA",
            taxabilityProfile = NonWageTaxableProfile(),
            sourceDestPairs = listOf(Jane.iraAcct to Household.investAcct)
        ),
        CloseAccountsOnYear(
            year = 2050, transferName = "Jane-Death-Roth",
            taxabilityProfile = NonTaxableProfile(),
            sourceDestPairs = listOf(Jane.rothAcct to Household.investAcct)
        ),
        CloseAccountsOnYear(
            year = 2050, transferName = "Connie-Death",
            taxabilityProfile = NonWageTaxableProfile(),
            sourceDestPairs = listOf(Richard.iraAcct to Household.investAcct)
        )
    )

    override fun simSuccess(): SimSuccess = SimSuccess {
        it.assetValue / it.inflation > 1000000.0
    }
}