package socsec

import RecIdentifier
import income.incomeRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import tax.TaxableAmounts
import util.currentDate
import yearlyDetailFixture

class NewIncomeAdjustBaseAmountProviderTest : ShouldSpec({
    val yearPrev = currentDate.year +1
    val startAmount = 20000.0
    val incPer100k = 500.0
    val provider = NewIncomeAdjustBaseAmountProvider(startAmount, incPer100k)

    val ident = RecIdentifier(name = "Primary", person = "Person")

    should("baseAmount is start amount if null prev year or null prev rec") {
        provider.baseAmount(prevRec = null, prevYear = null)
            .shouldBe(startAmount)

        provider.baseAmount(prevRec = null, prevYear = yearlyDetailFixture(yearPrev))
            .shouldBe(startAmount)
    }

    should("baseAmount is prev rec baseAmount + (newIncome / 100k) * incPer100k") {
        val prevBaseAmount = 25000.0
        val prevRec = benefitsRecFixture(year = yearPrev, baseAmount = prevBaseAmount)

        val ssIncome = 50000.0
        val incomes = listOf(
            incomeRecFixture(year = yearPrev, person = ident.person, amount = 1234.0),
            incomeRecFixture(year = yearPrev, person = "Other Person", amount = 5678.0),
            incomeRecFixture(
                year = yearPrev, person = ident.person, amount = ssIncome,
                taxableAmounts = TaxableAmounts(person = ident.person, socSec = ssIncome)),
        )

        val prevYear = yearlyDetailFixture(year = yearPrev,
            benefits = listOf(prevRec), incomes = incomes)

        provider.baseAmount(prevRec = prevRec, prevYear = prevYear)
            .shouldBe(prevBaseAmount + (ssIncome / 100000.0 * incPer100k))
    }
})
