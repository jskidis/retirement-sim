package socsec

import Rate
import RecIdentifier
import YearMonth
import YearlyDetail
import config.Person
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import tax.TaxabilityProfile
import util.currentDate

// TODO: convert from interfaces to delagation members
abstract class PrimarySSBenefitProgression(
    val person: Person,
    val taxabilityProfile: TaxabilityProfile
) : SSBenefitProgression,
    BenefitBaseAmountProvider,
    BenefitsClaimDateProvider,
    BenefitAdjustmentCalc,
    DefaultAdjustmentProvider,
    CmpdInflationProvider by StdCmpdInflationProvider() {

    override fun isPrimary(): Boolean = true
    override fun initialAdjustment(): Rate = 0.0
    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth)
        : Rate = StdBenefitAdjustmentCalc.calcBenefitAdjustment(birthYM, startYM)

    companion object {
        const val IDENT_NAME = "SSPrimary"
    }
    val ident = RecIdentifier(name = IDENT_NAME, person = person.name)

    override fun determineNext(prevYear: YearlyDetail?): SSBenefitRec {
        val year = (prevYear?.year?.let { it + 1 } ?: currentDate.year)
        val cmpInflation = getCmpdInflationEnd(prevYear)

        val prevRec = prevYear?.benefits?.find { it.ident == ident }
        val targetStart = claimDate(prevRec, prevYear)
        val hasClaimed = targetStart.year < year || (prevRec?.claimDate != null)
        val newClaim = !hasClaimed && targetStart.year == year

        val benefitAdj =
            if (!newClaim) prevRec?.benefitAdjustment ?: initialAdjustment()
            else calcBenefitAdjustment(person.birthYM, targetStart)

        val pctInYear =
            if (!newClaim) 1.0
            else 1 - targetStart.monthFraction()

        val initialClaim =
            if (newClaim) targetStart
            else prevRec?.claimDate

        val baseAmount = baseAmount(prevRec, prevYear)
        val value = benefitAdj * baseAmount * cmpInflation * pctInYear

        return SSBenefitRec(
            year = year,
            ident = ident,
            amount = value,
            taxableAmount = taxabilityProfile.calcTaxable(ident.person, value),
            baseAmount = baseAmount,
            benefitAdjustment = benefitAdj,
            claimDate = initialClaim
        )
    }
}
