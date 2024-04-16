package socsec

import Amount
import RecIdentifier
import YearMonth
import YearlyDetail
import config.Person
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import tax.TaxabilityProfile
import util.currentDate

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
        val year = (prevYear?.year?.let { it + 1 } ?: currentDate.year)
        val cmpInflation = cmpdInflationProvider.getCmpdInflationEnd(prevYear)

        val prevRec = prevYear?.benefits?.find { it.ident == ident }
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
            benefitAdj * baseAmount * cmpInflation * pctInYear)

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
