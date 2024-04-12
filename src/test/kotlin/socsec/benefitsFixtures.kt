package socsec

import Amount
import Name
import Rate
import RecIdentifier
import Year
import YearlyDetail
import tax.TaxableAmounts
import util.currentDate
import util.yearFromPrevYearDetail

fun benefitsRecFixture(
    year: Year = currentDate.year + 1,
    name: Name = "Benefit",
    person: Name = "Person",
    amount: Amount = 0.0,
    baseAmount: Amount = 0.0,
    benefitAdjustment: Rate = 0.0,
) = SSBenefitRec(
    year = year,
    ident = RecIdentifier(name, person),
    amount = amount,
    taxableAmount = TaxableAmounts(person),
    baseAmount = baseAmount,
    benefitAdjustment = benefitAdjustment
)

fun benefitsProgressionFixture(
    name: Name = "Income",
    person: Name = "Person",
    amount: Amount = 0.0
): SSBenefitProgression {
    return SSBenefitProgressionFixture(
        ident = RecIdentifier(name, person),
        amount = amount
    )
}

class SSBenefitProgressionFixture(
    val amount: Amount, val ident: RecIdentifier,
) : SSBenefitProgression {
    override fun determineNext(prevYear: YearlyDetail?): SSBenefitRec =
        SSBenefitRec(yearFromPrevYearDetail(prevYear), ident, amount, TaxableAmounts(ident.person))
}
