package socsec

import Amount
import Name
import Rate
import RecIdentifier
import Year
import YearMonth
import YearlyDetail
import config.Person
import config.personFixture
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
    claimDate: YearMonth? = null
) = SSBenefitRec(
    year = year,
    ident = RecIdentifier(name, person),
    amount = amount,
    taxableAmount = TaxableAmounts(person),
    baseAmount = baseAmount,
    benefitAdjustment = benefitAdjustment,
    claimDate = claimDate
)

fun benefitsProgressionFixture(): SSBenefitProgression {
    return SSBenefitProgressionFixture()
}

class SSBenefitProgressionFixture(
    person: Person = personFixture(birthYM = YearMonth(currentDate.year - 65)),
    taxabilityProfile: TaxabilityProfile = SSBenefitTaxableProfile(),
    val amount: Amount = 0.0,
    val targetDate: YearMonth = YearMonth(currentDate.year),
    val benefitAdj: Double = 1.0
) : PrimarySSBenefitProgression(person, taxabilityProfile) {

    override fun isPrimary(): Boolean  = true
    override fun baseAmount(prevRec: SSBenefitRec?, prevYear: YearlyDetail?) = amount
    override fun claimDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?) = targetDate
    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Rate = benefitAdj
    override fun initialAdjustment(): Rate = 0.0
}
