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
            spendAllocHandler = BasicSpendAlloc(),
            progression = AssetProgression(
                startBalance = Smiths.savingsBal,
                config = jointSavingConfig,
                gainCreator = SimpleAssetGainCreator(),
                attributesSet = listOf(
                    YearlyAssetAttributes(
                        startYear = Smiths.startYear - 1,
                        attributes = AssetAttributeMap.assetComp("US Cash")
                    )
                )
            )
        )

        val jointInvestConfig = AssetConfig(
            name = Smiths.investAcctName,
            person = "Jane & Dick",
            taxabilityProfile = OverriddenTaxableProfile(),
        )
        val jointInvest = AssetConfigProgression(
            config = jointInvestConfig,
            spendAllocHandler = BasicSpendAlloc(),
            progression = AssetProgression(
                startBalance = Smiths.investBal,
                config = jointInvestConfig,
                gainCreator = TaxableInvestGainCreator(),
                attributesSet = listOf(
                    YearlyAssetAttributes(
                        startYear = Smiths.startYear - 1,
                        attributes = AssetAttributeMap.assetComp("US Stocks")
                    )
                )
            )
        )

        return listOf(jointSavings, jointInvest)
    }
}