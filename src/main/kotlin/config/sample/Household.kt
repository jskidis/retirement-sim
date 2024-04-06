package config.sample

import Amount
import RecIdentifier
import asset.AssetProgression
import asset.SimpleAssetGainCreator
import asset.TaxableInvestGainCreator
import config.AssetAttributeMap
import config.HouseholdConfigBuilder
import expense.BasicExpenseProgression
import expense.ExpenseConfig
import expense.ExpenseConfigProgression
import inflation.StdInflationAmountAdjuster
import tax.NonTaxableProfile
import tax.NonWageTaxableProfile
import util.YearBasedConfig
import util.YearConfigPair

object Household : HouseholdConfigBuilder {
    val expenseStart: Amount = 25000.0

    val savingsAcct = RecIdentifier(name = "Savings", person = "Household")
    val savingsBal: Amount = 50000.0

    val investAcct = RecIdentifier(name = "BigInvBank", person = "Household")
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

    override fun assets(): List<AssetProgression> {

        val jointSavings = AssetProgression(
            ident = savingsAcct,
            startBalance = savingsBal,
            gainCreator = SimpleAssetGainCreator(
                taxability = NonWageTaxableProfile(),
                attributesSet = YearBasedConfig(
                    listOf(
                        YearConfigPair(
                            startYear = Smiths.startYear - 1,
                            config = AssetAttributeMap.assetComp("US Cash")
                        )
                    ))
            )
        )

        val jointInvest = AssetProgression(
            ident = investAcct,
            startBalance = investBal,
            gainCreator = TaxableInvestGainCreator(
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