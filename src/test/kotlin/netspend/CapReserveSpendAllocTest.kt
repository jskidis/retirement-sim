package netspend

import Amount
import RecIdentifier
import YearlyDetail
import asset.AssetRec
import asset.assetRecFixture
import expense.expenseRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import socsec.benefitsRecFixture
import util.SingleYearBasedConfig
import util.YearBasedConfig
import util.currentDate
import yearlyDetailFixture

class CapReserveSpendAllocTest : ShouldSpec({

    val year = currentDate.year + 1
    val currYear = yearlyDetailFixture(year)

    val target = 100000.0
    val margin = .05
    val handlerFixture = CapReserveSpendAllocFixture(target, margin)

    should("withdraw will withdraw full amount if balance is higher than target amount and is less than difference between balance and target") {
        val withdrawAmount = 1000.0
        val amountOverTarget = 5000.0
        val startBal = target + amountOverTarget
        val assetRec = assetRecFixture(year, startBal = startBal)

        val result = handlerFixture.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(withdrawAmount)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.finalBalance().shouldBe(startBal - withdrawAmount)
    }

    should("withdraw will withdraw only until target is hit if balance is higher than target amount but by less than withdraw amount ") {
        val withdrawAmount = 10000.0
        val amountOverTarget = 5000.0
        val startBal = target + amountOverTarget
        val assetRec = assetRecFixture(year, startBal = startBal)

        val result = handlerFixture.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(amountOverTarget)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.finalBalance().shouldBe(target)
    }

    should("withdraw will not withdraw anything if balance is below target but above floor [target * (1-margin)]") {
        val withdrawAmount = 10000.0
        val startBal = target - 10.0
        val assetRec = assetRecFixture(year, startBal = startBal)

        val result = handlerFixture.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(0.0)

        assetRec.tributions.shouldHaveSize(0)
        assetRec.finalBalance().shouldBe(startBal)
    }

    should("withdraw will create a deposit instead if balance already below floor [target * (1-margin)]") {
        val withdrawAmount = 10000.0
        val amountBelowFloor = 100.0
        val startBal = target * (1 - margin) - amountBelowFloor
        val distanceToTarget = target - startBal
        val assetRec = assetRecFixture(year, startBal = startBal)

        val result = handlerFixture.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(-distanceToTarget)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(distanceToTarget)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.DEPOSIT)
        assetRec.finalBalance().shouldBe(target)
    }

    should("deposit will deposit full amount if balance is lower than target amount and is less than difference between balance and target") {
        val depositAmount = 1000.0
        val amountUnderTarget = 5000.0
        val startBal = target - amountUnderTarget
        val assetRec = assetRecFixture(year, startBal = startBal)

        val result = handlerFixture.deposit(depositAmount, assetRec, currYear)
        result.shouldBe(depositAmount)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.DEPOSIT)
        assetRec.finalBalance().shouldBe(startBal + depositAmount)
    }

    should("deposit will deposit only until target is hit if balance is lower than target amount but by less than deposit amount ") {
        val depositAmount = 10000.0
        val amountUnderTarget = 5000.0
        val startBal = target - amountUnderTarget
        val assetRec = assetRecFixture(year, startBal = startBal)

        val result = handlerFixture.deposit(depositAmount, assetRec, currYear)
        result.shouldBe(amountUnderTarget)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.DEPOSIT)
        assetRec.finalBalance().shouldBe(target)
    }

    should("deposit will not deposit anything if balance is above target but below ceiling [target * (1+margin)]") {
        val depositAmount = 10000.0
        val startBal = target + 10.0
        val assetRec = assetRecFixture(year, startBal = startBal)

        val result = handlerFixture.deposit(depositAmount, assetRec, currYear)
        result.shouldBe(0.0)

        assetRec.tributions.shouldHaveSize(0)
        assetRec.finalBalance().shouldBe(startBal)
    }

    should("deposit will create a withdraw instead if balance already above ceiling [target * (1+margin)]") {
        val depositAmount = 10000.0
        val amountAboveCeiling = 100.0
        val startBal = target * (1 + margin) + amountAboveCeiling
        val distanceToTarget = startBal - target
        val assetRec = assetRecFixture(year, startBal = startBal)

        val result = handlerFixture.deposit(depositAmount, assetRec, currYear)
        result.shouldBe(-distanceToTarget)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-distanceToTarget)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.finalBalance().shouldBe(target)
    }

    should("determine target will multiply [expenses - benefits] by multiplier ") {
        val multiplier = 2.0
        val hanlder = CapReserveSpendAlloc(
            margin = .05,
            yearlyTargetMult = SingleYearBasedConfig(multiplier)
        )

        val currBalance = 10000.0
        val expenseTotal = 100000.0
        val benefitTotal = 1000.0
        val otherAssetsValue = expenseTotal * 4
        val expensesRec = expenseRecFixture(amount = expenseTotal)
        val benefitsRec = benefitsRecFixture(amount = benefitTotal)
        val assetRec = assetRecFixture(
            ident = RecIdentifier(name = "CapReserve", person = "Person"),
            startBal = currBalance
        )
        val otherAssetsRec = assetRecFixture(
            ident = RecIdentifier(name = "OtherAssets", person = "Person"),
            startBal = otherAssetsValue
        )

        val currYearCopy = currYear.copy(
            expenses = listOf(expensesRec),
            benefits = listOf(benefitsRec),
            assets = listOf(assetRec, otherAssetsRec)
        )

        val result = hanlder.determineTarget(currYearCopy, assetRec)
        result.shouldBe((expenseTotal - benefitTotal) * multiplier)
    }

    should("determine target will return 0 if other assets don't have enough to cover expense - benefits ") {
        val multiplier = 2.0
        val hanlder = CapReserveSpendAlloc(
            margin = .05,
            yearlyTargetMult = SingleYearBasedConfig(multiplier)
        )

        val currBalance = 10000.0
        val expenseTotal = 100000.0
        val benefitTotal = 1000.0
        val otherAssetsValue = expenseTotal - benefitTotal - 100.0
        val expensesRec = expenseRecFixture(amount = expenseTotal)
        val benefitsRec = benefitsRecFixture(amount = benefitTotal)
        val assetRec = assetRecFixture(
            ident = RecIdentifier(name = "CapReserve", person = "Person"),
            startBal = currBalance
        )
        val otherAssetsRec = assetRecFixture(
            ident = RecIdentifier(name = "OtherAssets", person = "Person"),
            startBal = otherAssetsValue
        )

        val currYearCopy = currYear.copy(
            expenses = listOf(expensesRec),
            benefits = listOf(benefitsRec),
            assets = listOf(assetRec, otherAssetsRec)
        )

        val result = hanlder.determineTarget(currYearCopy, assetRec)
        result.shouldBeZero()
    }
})

class CapReserveSpendAllocFixture(val target: Amount, margin: Double) :
    CapReserveSpendAlloc(YearBasedConfig(listOf()), margin) {

    override fun determineTarget(currYear: YearlyDetail, assetRec: AssetRec): Amount  = target
}