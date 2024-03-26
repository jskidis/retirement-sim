package config.sample

import Amount
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
    val expenseStart: Amount = 25000.0

    val savingsAcctName = "Savings"
    val savingsBal: Amount = 50000.0

    val investAcctName = "BigInvBank"
    val investBal = 200000.0


    override fun expenses(): List<ExpenseConfigProgression> {
        val householdExpensesConfig = ExpenseConfig(
            name = "Expenses", person = "Household",
            taxabilityProfile = NonTaxableProfile()
        )
        return listOf(
            ExpenseConfigProgression(
                config = householdExpensesConfig,
                progression = BasicExpenseProgression(
                    startAmount = expenseStart,
                    config = householdExpensesConfig,
                    adjusters = listOf(StdInflationAmountAdjuster())
                )
            ))
    }

    override fun assets(): List<AssetConfigProgression> {

        val jointSavingConfig = AssetConfig(
            name = savingsAcctName,
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
                        YearConfigPair(Jane.employmentDates.end.year, 3.0),
                        YearConfigPair(Jane.targetSSDate.year, 4.0)
                    ))
            ),
            progression = AssetProgression(
                startBalance = savingsBal,
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
            name = investAcctName,
            person = "Jane & Dick",
            taxabilityProfile = OverriddenTaxableProfile(),
        )
        val jointInvest = AssetConfigProgression(
            config = jointInvestConfig,
            spendAllocHandler = TaxableInvestSpendAllocHandler(),
            progression = AssetProgression(
                startBalance = investBal,
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