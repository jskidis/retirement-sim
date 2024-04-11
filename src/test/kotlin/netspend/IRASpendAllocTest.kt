package netspend

import YearMonth
import asset.assetRecFixture
import config.ActuarialGender
import config.Person
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import tax.NonTaxableProfile
import tax.NonWageTaxableProfile
import util.ConstantsProvider
import util.currentDate
import yearlyDetailFixture

class IRASpendAllocTest : ShouldSpec({

    val year = currentDate.year + 1
    val retirementAge = ConstantsProvider.getValue(ConstantsProvider.KEYS.RETIREMENT_WITHDRAW_AGE)
    val currYear = yearlyDetailFixture(year)

    val person60 = Person(
        name = "60 YO",
        birthYM = YearMonth(year = year - retirementAge.toInt() - 1, month = 0),
        actuarialGender = ActuarialGender.FEMALE
    )

    val person55 = Person(
        name = "55 YO",
        birthYM = YearMonth(year = year - retirementAge.toInt() + 5, month = 0),
        actuarialGender = ActuarialGender.FEMALE
    )


    should("withdraw will withdraw full amount if balance is greater withdraw amount") {
        val withdrawAmount = 1000.0
        val startBal = 2000.0
        val assetRec = assetRecFixture(year, startBal = startBal)

        val handler = IRASpendAlloc(person60, NonWageTaxableProfile())

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(withdrawAmount)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.tributions[0].taxable?.fed.shouldBe(withdrawAmount)
        assetRec.tributions[0].taxable?.state.shouldBe(withdrawAmount)
        assetRec.finalBalance().shouldBe(startBal - withdrawAmount)
    }

    should("withdraw will withdraw full balance if balance is then withdraw amount") {
        val withdrawAmount = 4000.0
        val startBal = 2000.0
        val assetRec = assetRecFixture(year, startBal = startBal)

        val handler = IRASpendAlloc(person60, NonWageTaxableProfile())

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(startBal)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.tributions[0].taxable?.fed.shouldBe(startBal)
        assetRec.tributions[0].taxable?.state.shouldBe(startBal)
        assetRec.finalBalance().shouldBe(0.0)
    }

    should("withdraw will add 10% fed penalty if person under 60") {
        val withdrawAmount = 1000.0
        val startBal = 2000.0
        val assetRec = assetRecFixture(year, startBal = startBal)

        val handler = IRASpendAlloc(person55, NonWageTaxableProfile())

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(withdrawAmount)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.tributions[0].taxable?.fed.shouldBe(withdrawAmount * 1.1)
        assetRec.tributions[0].taxable?.state.shouldBe(withdrawAmount)
        assetRec.finalBalance().shouldBe(startBal - withdrawAmount)
    }

    should("withdraw will have no taxable amount if person is over 60 and tax profile is non-taxable") {
        val withdrawAmount = 1000.0
        val startBal = 2000.0
        val assetRec = assetRecFixture(year, startBal = startBal)

        val handler = IRASpendAlloc(person60, NonTaxableProfile())

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(withdrawAmount)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.tributions[0].taxable.shouldBeNull()
        assetRec.finalBalance().shouldBe(startBal - withdrawAmount)
    }

    should("withdraw will have only 10% fed penalty if person under 60 and tax profile (Roth) is NonTaxable") {
        val withdrawAmount = 1000.0
        val startBal = 2000.0
        val assetRec = assetRecFixture(year, startBal = startBal)

        val handler = IRASpendAlloc(person55, NonTaxableProfile())

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(withdrawAmount)

        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-result)
        assetRec.tributions[0].name.shouldBe(SpendAllocHandler.TributionNames.WITHDRAW)
        assetRec.tributions[0].taxable?.fed.shouldBe(withdrawAmount * 0.1)
        assetRec.tributions[0].taxable?.state.shouldBe(0.0)
        assetRec.finalBalance().shouldBe(startBal - withdrawAmount)
    }


    should("deposit will never deposit (must be done through contributions") {
        val startBal = 2000.0
        val assetRec = assetRecFixture(year, startBal = startBal)

        val handler = IRASpendAlloc(person60, NonWageTaxableProfile())
        val result = handler.deposit(100.0, assetRec, currYear)

        result.shouldBe(0.0)
        assetRec.tributions.shouldHaveSize(0)
        assetRec.finalBalance().shouldBe(startBal)
    }
})
