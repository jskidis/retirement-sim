package netspend

import Amount
import RecIdentifier
import Year
import YearlyDetail
import asset.AssetRec
import asset.assetRecFixture
import expense.expenseRecFixture
import income.incomeRecFixture
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import socsec.benefitsRecFixture
import tax.TaxesRec
import util.currentDate
import yearlyDetailFixture

class NetSpendAllocationTest : ShouldSpec({
    val year: Year = currentDate.year + 1

    val wdHandler1 = SpendAllocHandlerFixture(maxWithdraw = 1000.0)
    val assetConfig1 = NetSpendAssetConfig(
        ident = RecIdentifier("Asset 1", "person"),
        spendAllocHandler = wdHandler1
    )
    val assetRec1 = assetRecFixture(year = year, ident = assetConfig1.ident)

    val wdHandler2 = SpendAllocHandlerFixture(maxWithdraw = 2000.0, maxDeposit = 3000.0)
    val assetConfig2 = NetSpendAssetConfig(
        ident = RecIdentifier("Asset 2", "person"),
        spendAllocHandler = wdHandler2
    )
    val assetRec2 = assetRecFixture(year = year, ident = assetConfig2.ident)

    val spendAllocationConfig = NetSpendAllocationConfig(
        withdrawOrder = listOf(assetConfig1, assetConfig2),
        depositOrder = listOf(assetConfig2, assetConfig1))

    val inflation = inflationRecFixture()
    val income = incomeRecFixture(year, amount = 100000.0)
    val expense = expenseRecFixture(year, amount = 80000.0)
    val benefit = benefitsRecFixture(year, amount = 20000.0)
    val thisYearTaxes = TaxesRec(fed = 25000.0)
    val lastYearTaxes = TaxesRec(fed = 20000.0)
    val lastYearCarryOverTaxes = TaxesRec(fed = 30000.0)

    should("not call any handlers if net spend is 0") {
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec1, assetRec2))

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
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec1, assetRec2))

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
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec1, assetRec2))

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
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec1, assetRec2))

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
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec1, assetRec2))

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
        val currYear = yearlyDetailFixture(year = year, assets = listOf(assetRec1, assetRec2))

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

    should("determine net spend") {
        val currYear = yearlyDetailFixture(
            year = year,
            inflation = inflation,
            incomes = listOf(income),
            expenses = listOf(expense),
            benefits = listOf(benefit),
            taxes = thisYearTaxes
        )

        val prevYear = yearlyDetailFixture(
            year = year - 1,
            taxes = lastYearTaxes,
            secondPassTaxes = lastYearCarryOverTaxes
        )

        val expectedResult = income.amount() + benefit.amount() -
            expense.amount() - thisYearTaxes.total() -
            ((lastYearCarryOverTaxes.total() - lastYearTaxes.total()) * (1 + inflation.std.rate))

        NetSpendAllocation.determineNetSpend(currYear, prevYear).shouldBe(expectedResult)
    }

    should("determine net spend when prev year is null") {
        val currYear = yearlyDetailFixture(
            year = year,
            inflation = inflation,
            incomes = listOf(income),
            expenses = listOf(expense),
            benefits = listOf(benefit),
            taxes = thisYearTaxes
        )

        val expectedResult = income.amount() + benefit.amount() -
            expense.amount() - thisYearTaxes.total()

        NetSpendAllocation.determineNetSpend(currYear, null).shouldBe(expectedResult)
    }

    should("determine net spend when curr year is actually the current year") {
        val currYear = yearlyDetailFixture(
            year = currentDate.year,
            inflation = inflation,
            incomes = listOf(income),
            expenses = listOf(expense),
            benefits = listOf(benefit),
            taxes = thisYearTaxes
        )

        val expectedResult = (income.nonAccruedAmount() + benefit.nonAccruedAmount() -
            expense.nonAccruedAmount() - thisYearTaxes.nonAccruedTotal(currentDate.year))

        NetSpendAllocation.determineNetSpend(currYear, null).shouldBe(expectedResult)
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


