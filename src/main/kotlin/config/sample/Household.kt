package config.sample

import asset.*
import config.AssetAttributeMap
import config.HouseholdConfigBuilder
import expense.BasicExpenseProgression
import expense.ExpenseConfig
import expense.ExpenseConfigProgression
import inflation.StdInflationAmountAdjuster
import tax.NonTaxableProfile
import tax.NonWageTaxableProfile
import tax.OverriddenTaxableProfile
import util.YearBasedConfig
import util.YearConfigPair

object Household : HouseholdConfigBuilder {
    override fun expenses(): List<ExpenseConfigProgression> {
        val householdExpensesConfig = ExpenseConfig(
            name = "Expenses", person = "Household",
            taxabilityProfile = NonTaxableProfile()
        )
        return listOf(
            ExpenseConfigProgression(
                config = householdExpensesConfig,
                progression = BasicExpenseProgression(
                    startAmount = Smiths.houseExpStart,
                    config = householdExpensesConfig,
                    adjusters = listOf(StdInflationAmountAdjuster())
                )
            ))
    }

    override fun assets(): List<AssetConfigProgression> {

        val jointSavingConfig = AssetConfig(
            name = Smiths.savingsAcctName,
            person = "Jane & Dick",
            taxabilityProfile = NonWageTaxableProfile(),
        )
        val jointSavings = AssetConfigProgression(
            config = jointSavingConfig,
            spendAllocHandler = CapReserveSpendAlloc(
                margin = .05,
                yearlyTargetMult = YearBasedConfig(
                    listOf(
                        YearConfigPair(2024, 2.0),
                        YearConfigPair(Smiths.janeEmploymentDate.end.year + 1, 3.0),
                        YearConfigPair(Smiths.janeTargetCollectSSYM.year, 4.0)
                    ))
            ),
            progression = AssetProgression(
                startBalance = Smiths.savingsBal,
                config = jointSavingConfig,
                gainCreator = SimpleAssetGainCreator(),
                attributesSet = YearBasedConfig(
                    listOf(
                        YearConfigPair(
                            startYear = Smiths.startYear - 1,
                            config = AssetAttributeMap.assetComp("US Cash")
                        )
                    ))
            )
        )

        val jointInvestConfig = AssetConfig(
            name = Smiths.investAcctName,
            person = "Jane & Dick",
            taxabilityProfile = OverriddenTaxableProfile(),
        )
        val jointInvest = AssetConfigProgression(
            config = jointInvestConfig,
            spendAllocHandler = TaxableInvestSpendAllocHandler(),
            progression = AssetProgression(
                startBalance = Smiths.investBal,
                config = jointInvestConfig,
                gainCreator = TaxableInvestGainCreator(),
                attributesSet = YearBasedConfig(
                    listOf(
                        YearConfigPair(
                            startYear = Smiths.startYear - 1,
                            config = AssetAttributeMap.assetComp("US Stocks")
                        )
                    ))
            )
        )

        return listOf(jointSavings, jointInvest)
    }
}