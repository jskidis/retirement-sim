package asset

import Amount
import YearlyDetail
import expense.expenseRecFixture
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import socsec.benefitsRecFixture
import yearlyDetailFixture

class CapReserveSpendAllocTest : FunSpec({

    val year = 2099
    val currYear = yearlyDetailFixture(year)
    val assetConfig = assetConfigFixture()

    val target = 100000.0
    val margin = .05
    val handlerFixture = CapReserveSpendAllocFixture(target, margin)

    test("withdraw will withdraw full amount if balance is higher than target amount and is less than difference between balance and target") {
        val withdrawAmount = 1000.0
        val amountOverTarget = 5000.0
        val startBal = target + amountOverTarget
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val result = handlerFixture.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(withdrawAmount)

        assetRec.tributions.size.shouldBe(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.finalBalance().shouldBe(startBal - withdrawAmount)
    }

    test("withdraw will withdraw only until target is hit if balance is higher than target amount but by less than withdraw amount ") {
        val withdrawAmount = 10000.0
        val amountOverTarget = 5000.0
        val startBal = target + amountOverTarget
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val result = handlerFixture.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(amountOverTarget)

        assetRec.tributions.size.shouldBe(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.finalBalance().shouldBe(target)
    }

    test("withdraw will not withdraw anything if balance is below target but above floor [target * (1-margin)]") {
        val withdrawAmount = 10000.0
        val startBal = target - 10.0
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val result = handlerFixture.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(0.0)

        assetRec.tributions.size.shouldBe(0)
        assetRec.finalBalance().shouldBe(startBal)
    }

    test("withdraw will create a deposit instead if balance already below floor [target * (1-margin)]") {
        val withdrawAmount = 10000.0
        val amountBelowFloor = 100.0
        val startBal = target * (1 - margin) - amountBelowFloor
        val distanceToTarget = target - startBal
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val result = handlerFixture.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(-distanceToTarget)

        assetRec.tributions.size.shouldBe(1)
        assetRec.tributions[0].amount.shouldBe(distanceToTarget)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.DEPOSIT)
        assetRec.finalBalance().shouldBe(target)
    }

    test("deposit will deposit full amount if balance is lower than target amount and is less than difference between balance and target") {
        val depositAmount = 1000.0
        val amountUnderTarget = 5000.0
        val startBal = target - amountUnderTarget
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val result = handlerFixture.deposit(depositAmount, assetRec, currYear)
        result.shouldBe(depositAmount)

        assetRec.tributions.size.shouldBe(1)
        assetRec.tributions[0].amount.shouldBe(result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.DEPOSIT)
        assetRec.finalBalance().shouldBe(startBal + depositAmount)
    }

    test("deposit will deposit only until target is hit if balance is lower than target amount but by less than deposit amount ") {
        val depositAmount = 10000.0
        val amountUnderTarget = 5000.0
        val startBal = target - amountUnderTarget
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val result = handlerFixture.deposit(depositAmount, assetRec, currYear)
        result.shouldBe(amountUnderTarget)

        assetRec.tributions.size.shouldBe(1)
        assetRec.tributions[0].amount.shouldBe(result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.DEPOSIT)
        assetRec.finalBalance().shouldBe(target)
    }

    test("deposit will not deposit anything if balance is above target but below ceiling [target * (1+margin)]") {
        val depositAmount = 10000.0
        val startBal = target + 10.0
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val result = handlerFixture.deposit(depositAmount, assetRec, currYear)
        result.shouldBe(0.0)

        assetRec.tributions.size.shouldBe(0)
        assetRec.finalBalance().shouldBe(startBal)
    }

    test("deposit will create a withdraw instead if balance already above ceiling [target * (1+margin)]") {
        val depositAmount = 10000.0
        val amountAboveCeiling = 100.0
        val startBal = target * (1 + margin) + amountAboveCeiling
        val distanceToTarget = startBal - target
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val result = handlerFixture.deposit(depositAmount, assetRec, currYear)
        result.shouldBe(-distanceToTarget)

        assetRec.tributions.size.shouldBe(1)
        assetRec.tributions[0].amount.shouldBe(-distanceToTarget)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.finalBalance().shouldBe(target)
    }

    test("determine target will multiply [expenses - benefits] by multiplier ") {
        val multiplier = 2.0
        val hanlder = CapReserveSpendAlloc(
            margin = .05,
            yearlyTargetMult = listOf(2020 to multiplier)
        )

        val expenseTotal = 100000.0
        val benefitTotal = 1000.0
        val expensesRec = expenseRecFixture(amount = expenseTotal)
        val benefitsRec = benefitsRecFixture(amount = benefitTotal)

        val currYearCopy = currYear.copy(
            expenses = listOf(expensesRec),
            benefits = listOf(benefitsRec)
        )

        val result = hanlder.determineTarget(currYearCopy)
        result.shouldBe((expenseTotal - benefitTotal) * multiplier)
    }
})

class CapReserveSpendAllocFixture(val target: Amount, margin: Double) :
    CapReserveSpendAlloc(listOf(), margin) {

    override fun determineTarget(currYear: YearlyDetail): Amount  = target
}