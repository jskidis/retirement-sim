package socsec

import Amount
import Name
import Year
import YearlyDetail
import tax.TaxabilityProfileFixture
import tax.TaxableAmounts
import util.yearFromPrevYearDetail

fun benefitsConfigFixture(
    name: Name = "Benefit",
    person: Name = "Person",
) = SSBenefitConfig(name, person, TaxabilityProfileFixture())

fun benefitsConfigProgressFixture(
    name: Name = "Benefit",
    person: Name = "Person",
    amount: Amount = 0.0,
): SSBenefitConfigProgression {
    val config = SSBenefitConfig(name, person, TaxabilityProfileFixture())
    val progression = SSBenefitProgressionFixture(amount, person, config)
    return SSBenefitConfigProgression(config, progression)
}

fun benefitsRecFixture(
    year: Year = 2024,
    name: Name = "Benefit",
    person: Name = "Person",
    amount: Amount = 0.0,
) = SSBenefitRec(
    year = year,
    config = benefitsConfigFixture(name, person),
    amount = amount,
    taxableAmount = TaxableAmounts(person)
)

class SSBenefitProgressionFixture(
    val amount: Amount, val person: Name, val config: SSBenefitConfig,
) : SSBenefitProgression {
    override fun determineNext(prevYear: YearlyDetail?): SSBenefitRec =
        SSBenefitRec(yearFromPrevYearDetail(prevYear), config, amount, TaxableAmounts(person))
}

