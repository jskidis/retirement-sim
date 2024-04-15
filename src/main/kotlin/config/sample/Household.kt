package config.sample

import Amount
import RecIdentifier
import asset.AssetProgression
import asset.AssetType
import asset.SimpleAssetGainCreator
import asset.TaxableInvestGainCreator
import config.AssetAttributeMap
import config.HouseholdConfigBuilder
import expense.BasicExpenseProgression
import expense.ExpenseProgression
import inflation.StdInflationAmountAdjuster
import tax.NonDeductProfile
import tax.NonWageTaxableProfile
import util.YearBasedConfig
import util.YearConfigPair

object Household : HouseholdConfigBuilder {
    val expenseStart: Amount = 25000.0

    val savingsAcct = RecIdentifier(name = "Savings", person = "Household")
    val savingsBal: Amount = 50000.0

    val investAcct = RecIdentifier(name = "BigInvBank", person = "Household")
    val investBal = 200000.0


    override fun expenses(): List<ExpenseProgression> {
        return listOf(
            BasicExpenseProgression(
                ident = RecIdentifier(name = "Expenses", person = "Household"),
                startAmount = expenseStart,
                taxabilityProfile = NonDeductProfile(),
                adjusters = listOf(StdInflationAmountAdjuster())
            )
        )
    }

    override fun assets(): List<AssetProgression> {

        val jointSavings = AssetProgression(
            ident = savingsAcct,
            assetType = AssetType.CASH,
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
            assetType = AssetType.NRA,
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