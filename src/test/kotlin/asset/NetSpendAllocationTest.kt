package asset

import Amount
import Name
import expense.expenseRecFixture
import income.incomeRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class NetSpendAllocationTest : ShouldSpec({
    val yearInFuture = 2100

    fun genAssetRec(name: Name, type: AssetType, startBalance: Amount) : AssetRec {
        val config = assetConfigFixture(assetName = name, assetType = type)
        return assetRecFixture(assetConfig = config, startBal = startBalance)
    }

    fun genCurrYear(income: Amount, expenses: Amount, assets: List<AssetRec>) =
        yearlyDetailFixture(
            year = yearInFuture,
            incomes = listOf(incomeRecFixture(amount = income)),
            expenses = listOf(expenseRecFixture(amount = expenses)),
            assets = assets
        )

    should("Allocate loss when single asset") {
        val savings100k = genAssetRec("Savings-100K", AssetType.CASH, 100000.0)
        val currYear = genCurrYear(income = 50000.0, expenses = 60000.0,
            assets = listOf(savings100k)
        )

        val result = NetSpendAllocation.allocatedNetSpend(currYear)
        result.shouldBe(0.0)
        savings100k.finalBalance().shouldBeWithinPercentageOf(
            savings100k.startBal + currYear.netSpend(), .001)
    }

    should("Allocate loss across multiple assets of same class portionally to balance") {
        val savings90k = genAssetRec("Savings-90K", AssetType.CASH, 90000.0)
        val savings10k = genAssetRec("Savings-10K", AssetType.CASH, 10000.0)
        val currYear = genCurrYear(income = 50000.0, expenses = 60000.0,
            assets = listOf(savings90k, savings10k)
        )

        val result = NetSpendAllocation.allocatedNetSpend(currYear)
        result.shouldBe(0.0)
        savings90k.finalBalance().shouldBeWithinPercentageOf(
            savings90k.startBal + (0.9 * currYear.netSpend()), .001)
        savings10k.finalBalance().shouldBeWithinPercentageOf(
            savings10k.startBal + (0.1 * currYear.netSpend()), .001)
    }

    should("Allocate loss across multiple assets of same class proportionally to balance and rollover to next asset type is not sufficient to cover") {
        val savings4k = genAssetRec("Savings-4K", AssetType.CASH, 4000.0)
        val savings1k = genAssetRec("Savings-1K", AssetType.CASH, 1000.0)
        val invest90k = genAssetRec("Invest-90K", AssetType.INVEST, 90000.0)
        val invest10k = genAssetRec("Invest-10K", AssetType.INVEST, 10000.0)
        val currYear = genCurrYear(income = 50000.0, expenses = 60000.0,
            assets = listOf(savings4k, savings1k, invest90k, invest10k)
        )

        val result = NetSpendAllocation.allocatedNetSpend(currYear)
        result.shouldBe(0.0)
        savings4k.finalBalance().shouldBe(0.0)
        savings1k.finalBalance().shouldBe(0.0)
        invest90k.finalBalance().shouldBeWithinPercentageOf(
            invest90k.startBal + (0.5 * 0.9 * currYear.netSpend()), .001)
        invest10k.finalBalance().shouldBeWithinPercentageOf(
            invest10k.startBal + (0.5 * 0.1 * currYear.netSpend()), .001)
    }

    should("Will return negative amount if not enough assets to cover losses") {
        val savings5k = genAssetRec("Savings-4K", AssetType.CASH, 5000.0)
        val invest10k = genAssetRec("Invest-90K", AssetType.INVEST, 10000.0)
        val currYear = genCurrYear(income = 50000.0, expenses = 70000.0,
            assets = listOf(savings5k, invest10k)
        )

        val result = NetSpendAllocation.allocatedNetSpend(currYear)
        result.shouldBe(currYear.netSpend() + savings5k.startBal + invest10k.startBal)
        savings5k.finalBalance().shouldBe(0.0)
        invest10k.finalBalance().shouldBe(0.0)
    }

    should("Will allocate positive net spend") {
        val savings90k = genAssetRec("Savings-90K", AssetType.CASH, 90000.0)
        val savings10k = genAssetRec("Savings-10K", AssetType.CASH, 10000.0)
        val currYear = genCurrYear(income = 60000.0, expenses = 50000.0,
            assets = listOf(savings90k, savings10k)
        )

        val result = NetSpendAllocation.allocatedNetSpend(currYear)
        result.shouldBe(0.0)
        savings90k.finalBalance().shouldBeWithinPercentageOf(
            savings90k.startBal + (0.9 * currYear.netSpend()), .001)
        savings10k.finalBalance().shouldBeWithinPercentageOf(
            savings10k.startBal + (0.1 * currYear.netSpend()), .001)
    }
})

