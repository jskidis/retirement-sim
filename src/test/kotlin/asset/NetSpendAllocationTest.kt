package asset

import Amount
import Year
import YearlyDetail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class NetSpendAllocationTest : ShouldSpec({
    val year: Year = 2020

    val wdHandler1 = SpendAllocHandlerFixture(maxWithdraw = 1000.0)
    val assetConfig1 = assetConfigProgressFixture(
        name = "Asset 1", spendAllocHandler = wdHandler1
    )
    val assetRec1 = assetRecFixture(
        year = year, assetConfig = assetConfig1.config
    )

    val wdHandler2 = SpendAllocHandlerFixture(maxWithdraw = 2000.0, maxDeposit = 3000.0)
    val assetConfig2 = assetConfigProgressFixture(
        name = "Asset 2", spendAllocHandler = wdHandler2)
    val assetRec2 = assetRecFixture(
        year = year, assetConfig = assetConfig2.config
    )

    val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec1, assetRec2))

    val spendAllocationConfig = NetSpendAllocationConfig(
        withdrawOrder = listOf(assetConfig1, assetConfig2),
        depositOrder = listOf(assetConfig2, assetConfig1))

    should("not call any handlers if net spend is 0") {
        wdHandler1.reset()
        wdHandler2.reset()
        val result = NetSpendAllocation.allocateNetSpend(
            netSpend = 0.0, currYear, spendAllocationConfig)

        result.shouldBe(0.0)
        wdHandler1.wasWithdrawCalled.shouldBeFalse()
        wdHandler1.wasDepositCalled.shouldBeFalse()

        wdHandler2.wasWithdrawCalled.shouldBeFalse()
        wdHandler2.wasDepositCalled.shouldBeFalse()
    }

    should("withdraw only from first asset if net spend is less than max withdraw on first asset") {
        wdHandler1.reset()
        wdHandler2.reset()
        val result = NetSpendAllocation.allocateNetSpend(
            netSpend = -wdHandler1.maxWithdraw / 2, currYear, spendAllocationConfig)

        result.shouldBe(0.0)

        wdHandler1.wasWithdrawCalled.shouldBeTrue()
        wdHandler1.lastWithdraw.shouldBe(wdHandler1.maxWithdraw / 2)
        wdHandler1.wasDepositCalled.shouldBeFalse()

        wdHandler2.wasWithdrawCalled.shouldBeFalse()
        wdHandler2.wasDepositCalled.shouldBeFalse()
    }

    should("withdraw from first & second asset if net spend is more than max withdraw of first asset") {
        wdHandler1.reset()
        wdHandler2.reset()
        val result = NetSpendAllocation.allocateNetSpend(
            netSpend = -wdHandler1.maxWithdraw - 10.0, currYear, spendAllocationConfig)

        result.shouldBe(0.0)

        wdHandler1.wasWithdrawCalled.shouldBeTrue()
        wdHandler1.lastWithdraw.shouldBe(wdHandler1.maxWithdraw)
        wdHandler1.wasDepositCalled.shouldBeFalse()

        wdHandler2.wasWithdrawCalled.shouldBeTrue()
        wdHandler2.lastWithdraw.shouldBe(10.0)
        wdHandler2.wasDepositCalled.shouldBeFalse()
    }

    should("return the amount left over after withdrawing max amounts from each asset") {
        wdHandler1.reset()
        wdHandler2.reset()
        val result = NetSpendAllocation.allocateNetSpend(
            netSpend = -wdHandler1.maxWithdraw - wdHandler2.maxWithdraw - 10.0,
            currYear, spendAllocationConfig)

        result.shouldBe(-10.0)

        wdHandler1.wasWithdrawCalled.shouldBeTrue()
        wdHandler1.lastWithdraw.shouldBe(wdHandler1.maxWithdraw)
        wdHandler1.wasDepositCalled.shouldBeFalse()

        wdHandler2.wasWithdrawCalled.shouldBeTrue()
        wdHandler2.lastWithdraw.shouldBe(wdHandler2.maxWithdraw)
        wdHandler2.wasDepositCalled.shouldBeFalse()
    }

    should("deposit only into first asset if net spend is less than max deposit on second asset") {
        wdHandler1.reset()
        wdHandler2.reset()
        val result = NetSpendAllocation.allocateNetSpend(
            netSpend = wdHandler2.maxDeposit / 2, currYear, spendAllocationConfig)

        result.shouldBe(0.0)

        wdHandler2.wasDepositCalled.shouldBeTrue()
        wdHandler2.lastDeposit.shouldBe(wdHandler2.maxDeposit / 2)
        wdHandler2.wasWithdrawCalled.shouldBeFalse()

        wdHandler1.wasWithdrawCalled.shouldBeFalse()
        wdHandler1.wasDepositCalled.shouldBeFalse()
    }

    should("deposit from first & second asset if net spend is more than max deposit of second asset") {
        wdHandler1.reset()
        wdHandler2.reset()
        val result = NetSpendAllocation.allocateNetSpend(
            netSpend = wdHandler2.maxDeposit + 10.0, currYear, spendAllocationConfig)

        result.shouldBe(0.0)

        wdHandler2.wasDepositCalled.shouldBeTrue()
        wdHandler2.lastDeposit.shouldBe(wdHandler2.maxDeposit)
        wdHandler2.wasWithdrawCalled.shouldBeFalse()

        wdHandler1.wasDepositCalled.shouldBeTrue()
        wdHandler1.lastDeposit.shouldBe(10.0)
        wdHandler1.wasWithdrawCalled.shouldBeFalse()
    }
})

class SpendAllocHandlerFixture(
    val maxWithdraw: Amount = Amount.MAX_VALUE,
    val maxDeposit: Amount = Amount.MAX_VALUE,
) : SpendAllocHandler {
    var wasWithdrawCalled = false
    var wasDepositCalled = false
    var lastWithdraw: Amount = 0.0
    var lastDeposit: Amount = 0.0

    override fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        val withdrawAmount = Math.min(maxWithdraw, amount)
        lastWithdraw = withdrawAmount
        wasWithdrawCalled = true
        return withdrawAmount
    }

    override fun deposit(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        val depositAmount = Math.min(maxDeposit, amount)
        lastDeposit = depositAmount
        wasDepositCalled = true
        return depositAmount
    }

    fun reset() {
        wasWithdrawCalled = false
        wasDepositCalled = false
        lastWithdraw = 0.0
        lastDeposit = 0.0
    }
}


/*
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
*/
