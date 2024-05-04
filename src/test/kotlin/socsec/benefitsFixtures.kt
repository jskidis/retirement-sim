package socsec

import Amount
import Name
import Rate
import RecIdentifier
import Year
import YearMonth
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

fun secondaryProgressionFixture(): SecondarySSBenefitProgression {
    return SecondarySSBenefitProgressionFixture()
}

class SSBenefitProgressionFixture(
    person: Person = personFixture(birthYM = YearMonth(currentDate.year - 65)),
    taxabilityProfile: TaxabilityProfile = SSBenefitTaxableProfile(),
    amount: Amount = 0.0,
    targetDate: YearMonth = YearMonth(currentDate.year),
    benefitAdj: Double = 1.0
) : PrimarySSBenefitProgression(
        person = person,
        taxabilityProfile = taxabilityProfile,
        baseAmount = amount,
        targetYM = targetDate,
        benefitAdjCalc = BenefitAdjustmentCalc { _, _ -> benefitAdj }
)

class SecondarySSBenefitProgressionFixture(
    person: Person = personFixture(name = "Person", birthYM = YearMonth(currentDate.year - 65)),
    spouse: Person = personFixture(name = "Spouse", birthYM = YearMonth(currentDate.year - 65)),
    taxabilityProfile: TaxabilityProfile = SSBenefitTaxableProfile(),
    benefitAdj: Double = 1.0
) : SpousalSSBenefitProgression(
    person = person,
    provider = spouse,
    taxabilityProfile = taxabilityProfile,
    benefitAdjCalc = BenefitAdjustmentCalc { _, _ -> benefitAdj }
)
