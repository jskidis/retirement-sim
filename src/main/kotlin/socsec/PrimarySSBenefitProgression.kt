package socsec

import Amount
import RecIdentifier
import YearMonth
import YearlyDetail
import config.Person
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import tax.TaxabilityProfile
import util.RecFinder
import util.currentDate
import util.yearFromPrevYearDetail

open class PrimarySSBenefitProgression(
    val person: Person,
    val taxabilityProfile: TaxabilityProfile,
    val baseAmount: Amount = 0.0,
    val baseAmountProvider: BenefitBaseAmountProvider = StdBenefitBaseAmountProvider(baseAmount),
    val targetYM: YearMonth = maxOf(YearMonth(currentDate.year), person.birthYM.copy(year = person.birthYM.year + 62)),
    val claimDateProvider: BenefitsClaimDateProvider = StdBenefitsClaimDateProvider(targetYM),
    val benefitAdjCalc: BenefitAdjustmentCalc = StdBenefitAdjustmentCalc,
    val defaultAdjProvider: DefaultAdjustmentProvider = StdDefaultAdjustmentProvider(),
    val payoutAdjProvider: PayoutAdjProvider = StdPayoutAdjProvider(),
    val cmpdInflationProvider: CmpdInflationProvider = StdCmpdInflationProvider()
) : SSBenefitProgression {

    companion object { const val IDENT_NAME = "SSPrimary" }
    val ident = RecIdentifier(name = IDENT_NAME, person = person.name)

    override fun determineNext(prevYear: YearlyDetail?): SSBenefitRec {
        val year = yearFromPrevYearDetail(prevYear)
        val cmpInflation = cmpdInflationProvider.getCmpdInflationEnd(prevYear)

        val prevRec = prevYear?.let { RecFinder.findBenefitRec(ident, prevYear) }
        val targetStart = claimDateProvider.claimDate(prevRec, prevYear)
        val hasClaimed = targetStart.year < year || (prevRec?.claimDate != null)
        val newClaim = !hasClaimed && targetStart.year == year

        val benefitAdj =
            if (!newClaim) prevRec?.benefitAdjustment ?: defaultAdjProvider.initialAdjustment()
            else benefitAdjCalc.calcBenefitAdjustment(person.birthYM, targetStart)

        val pctInYear =
            if (!newClaim) 1.0
            else 1 - targetStart.monthFraction()

        val initialClaim =
            if (newClaim && benefitAdj > 0.0) targetStart
            else prevRec?.claimDate

        val baseAmount = baseAmountProvider.baseAmount(prevRec, prevYear)
        val value = payoutAdjProvider.adjustPayout(
            benefitAdj * baseAmount * cmpInflation * pctInYear, year)

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
