package socsec

import Amount
import RecIdentifier
import Year
import YearMonth
import asset.assetRecFixture
import expense.expenseRecFixture
import income.incomeRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import tax.TaxableAmounts
import yearlyDetailFixture

class FlexibleClaimDateProviderTest : ShouldSpec({
    val birthYM = YearMonth(1965)
    val targetYM = YearMonth(2035)
    val expenseMultiple = 5.0

    val provider = FlexibleClaimDateProvider(birthYM, targetYM, expenseMultiple)

    val ident = RecIdentifier(name = "Primary", person = "Person")

    fun incomes(year: Year, amount: Amount) = listOf(
        incomeRecFixture(year = year, person = ident.person, amount = 1234.0),
        incomeRecFixture(year = year, person = "Other Person", amount = 5678.0),
        incomeRecFixture(
            year = year, person = ident.person, amount = amount,
            taxableAmounts = TaxableAmounts(person = ident.person, socSec = amount)),
    )

    should("claimDate is original target if null prev year, null prev rec, target date is this year or age is < 63") {
        provider.claimDate(prevRec = null, prevYear = null)
            .shouldBe(targetYM)

        provider.claimDate(prevRec = null, prevYear = yearlyDetailFixture(2030))
            .shouldBe(targetYM)

        provider.claimDate(
            prevRec = benefitsRecFixture(targetYM.year - 1),
            prevYear = yearlyDetailFixture(targetYM.year - 1)
        ).shouldBe(targetYM)

        provider.claimDate(
            prevRec = benefitsRecFixture(birthYM.year + 60),
            prevYear = yearlyDetailFixture(birthYM.year + 60)
        ).shouldBe(targetYM)
    }

    should("claimDate is claimDate on prevRec if it's not null") {
        val prevClaimDate = YearMonth(birthYM.year + 65)
        val prevRec = benefitsRecFixture(birthYM.year + 67, claimDate = prevClaimDate)

        provider.claimDate(
            prevRec = prevRec,
            prevYear = yearlyDetailFixture(birthYM.year + 67, benefits = listOf(prevRec))
        ).shouldBe(prevClaimDate)
    }

    should("claimDate should be current year only if wage for person is less than base Amount, and over 62") {
        val yearPrev = birthYM.year + 65
        val baseAmount = 25000.0
        val prevRec = benefitsRecFixture(yearPrev, baseAmount = baseAmount)

        provider.claimDate(
            prevRec = prevRec,
            prevYear = yearlyDetailFixture(
                year = yearPrev,
                benefits = listOf(prevRec),
                incomes = incomes(yearPrev, baseAmount * 2))
        ).shouldBe(targetYM)

        provider.claimDate(
            prevRec = prevRec,
            prevYear = yearlyDetailFixture(
                year = yearPrev,
                benefits = listOf(prevRec),
                incomes = incomes(yearPrev, baseAmount / 2))
        ).shouldBe(YearMonth(yearPrev + 1))
    }

    should("claimDate should be current year only if total assets / expenses is less than provided ration, and over 62") {
        val yearPrev = birthYM.year + 65
        val baseAmount = 25000.0
        val expenses = 100000.0
        val otherBenefits = 20000.0
        val assetValue = 300000.0
        // asset to expense minus other benefits ratio is 2.5

        val expenseRec = expenseRecFixture(year = yearPrev, amount = expenses)
        val assetRec = assetRecFixture(year = yearPrev, startBal = assetValue)
        val otherBenefitRec = benefitsRecFixture(year = yearPrev, amount = otherBenefits)
        val prevRec = benefitsRecFixture(yearPrev, baseAmount = baseAmount)

        val prevYear = yearlyDetailFixture(
            yearPrev,
            expenses = listOf(expenseRec), assets = listOf(assetRec),
            benefits = listOf(prevRec, otherBenefitRec)
        )

        val provider2x = FlexibleClaimDateProvider(birthYM, targetYM, 2.0)
        provider2x.claimDate(prevRec = prevRec, prevYear = prevYear)
            .shouldBe(targetYM)

        val provider5x = FlexibleClaimDateProvider(birthYM, targetYM, 5.0)
        provider5x.claimDate(prevRec = prevRec, prevYear = prevYear)
            .shouldBe(YearMonth(yearPrev + 1))
    }
})
