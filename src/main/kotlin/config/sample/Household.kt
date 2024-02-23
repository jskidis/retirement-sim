package config.sample

import AssetType
import asset.*
import config.AssetCompConfig
import config.HouseholdConfigBuilder
import expense.BasicExpenseProgression
import expense.ExpenseConfig
import expense.ExpenseConfigProgression
import inflation.StdInflationAmountAdjuster
import tax.NonRetirementAssetTaxableProfile
import tax.NonTaxableProfile
import tax.NonWageTaxableProfile

object Household : HouseholdConfigBuilder {
    override fun expenses(): List<ExpenseConfigProgression> {
        val householdExpensesConfig = ExpenseConfig(
            name = "Expenses", person = "Household",
            taxabilityProfile = NonTaxableProfile()
        )
        return listOf(ExpenseConfigProgression(
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
            name = "Savings",
            person = "Jane & Dick",
            taxabilityProfile = NonWageTaxableProfile(),
            AssetType.CASH,
            minMaxProvider = NoMinMaxBalProvider(),
            assetCompMap = listOf(
                YearlyAssetComposition(Smiths.startYear -1,
                    listOf(AssetCompConfig.assetComp("US Cash"))
                )
            )
        )
        val jointSavings = AssetConfigProgression(
            config = jointSavingConfig,
            progression = BasicAssetProgression(
                Smiths.savingsBal, jointSavingConfig
            )
        )

        val jointInvestConfig = AssetConfig(
            name = "Big Inv Bank",
            person = "Jane & Dick",
            taxabilityProfile = NonRetirementAssetTaxableProfile(),
            AssetType.INVEST,
            minMaxProvider = NoMinMaxBalProvider(),
            assetCompMap = listOf(
                YearlyAssetComposition(Smiths.startYear -1,
                    listOf(AssetCompConfig.assetComp("US Stocks"))
                )
            )
        )
        val jointInvest = AssetConfigProgression(
            config = jointInvestConfig,
            progression = BasicAssetProgression(
                Smiths.investBal, jointInvestConfig)
        )

        return listOf(jointSavings, jointInvest)
    }
}