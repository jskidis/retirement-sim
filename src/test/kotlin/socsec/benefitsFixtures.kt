package socsec

import Amount
import Name
import Rate
import RecIdentifier
import Year
import YearMonth
import YearlyDetail
import tax.SSBenefitTaxableProfile
import tax.TaxabilityProfile
import tax.TaxableAmounts
import util.currentDate

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
    ident: RecIdentifier,
    birthYM: YearMonth = YearMonth(currentDate.year - 65),
    taxabilityProfile: TaxabilityProfile = SSBenefitTaxableProfile(),
    val amount: Amount = 0.0,
    val targetDate: YearMonth = YearMonth(currentDate.year),
    val benefitAdj: Double = 1.0
) : PrimarySSBenefitProgression(ident, birthYM, taxabilityProfile) {

    override fun isPrimary(): Boolean  = true
    override fun baseAmount(prevRec: SSBenefitRec?, prevYear: YearlyDetail?) = amount
    override fun targetDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?) = targetDate
    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Rate = benefitAdj
    override fun initialAdjustment(): Rate = 0.0
}
