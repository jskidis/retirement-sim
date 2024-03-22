package asset

import YearMonth
import config.ActuarialGender
import config.Person
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class IRASpendAllocTest : FunSpec({

    val year = 2024
    val currYear = yearlyDetailFixture(year)
    val assetConfig = assetConfigFixture()

    val person60 = Person(
        name = "60 YO",
        birthYM = YearMonth(year = year - 60, month = 0),
        actuarialGender = ActuarialGender.FEMALE
    )

    val person55 = Person(
        name = "55 YO",
        birthYM = YearMonth(year = year - 55, month = 0),
        actuarialGender = ActuarialGender.FEMALE
    )


    test("withdraw will withdraw full amount if balance is greater withdraw amount") {
        val withdrawAmount = 1000.0
        val startBal = 2000.0
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val handler = IRASpendAlloc(person60)

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(withdrawAmount)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.tributions[0].taxable?.fed.shouldBe(withdrawAmount)
        assetRec.tributions[0].taxable?.state.shouldBe(withdrawAmount)
        assetRec.finalBalance().shouldBe(startBal - withdrawAmount)
    }

    test("withdraw will withdraw full balance if balance is then withdraw amount") {
        val withdrawAmount = 4000.0
        val startBal = 2000.0
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val handler = IRASpendAlloc(person60)

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(startBal)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.tributions[0].taxable?.fed.shouldBe(startBal)
        assetRec.tributions[0].taxable?.state.shouldBe(startBal)
        assetRec.finalBalance().shouldBe(0.0)
    }

    test("withdraw will at 10% fed penalty if person under 60") {
        val withdrawAmount = 1000.0
        val startBal = 2000.0
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val handler = IRASpendAlloc(person55)

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(withdrawAmount)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.tributions[0].taxable?.fed.shouldBe(withdrawAmount * 1.1)
        assetRec.tributions[0].taxable?.state.shouldBe(withdrawAmount)
        assetRec.finalBalance().shouldBe(startBal - withdrawAmount)
    }

    test("deposit will never deposit (must be done through contributions") {
        val startBal = 2000.0
        val assetRec = assetRecFixture(year, assetConfig, startBal)

        val handler = IRASpendAlloc(person60)
        val result = handler.deposit(100.0, assetRec, currYear)

        result.shouldBe(0.0)
        assetRec.tributions.shouldHaveSize(0)
        assetRec.finalBalance().shouldBe(startBal)
    }
})
