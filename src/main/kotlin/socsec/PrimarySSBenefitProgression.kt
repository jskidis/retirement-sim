package socsec

import Rate
import RecIdentifier
import YearMonth
import YearlyDetail
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import tax.TaxabilityProfile
import util.currentDate

abstract class PrimarySSBenefitProgression(
    val ident: RecIdentifier,
    val birthYM: YearMonth,
    val taxabilityProfile: TaxabilityProfile
) : SSBenefitProgression,
    BenefitBaseAmountProvider,
    BenefitsTargetDateProvider,
    BenefitAdjustmentCalc,
    DefaultAdjustmentProvider,
    CmpdInflationProvider by StdCmpdInflationProvider() {

    override fun isPrimary(): Boolean = true
    override fun initialAdjustment(): Rate = 0.0
    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth)
        : Rate = StdBenefitAdjustmentCalc.calcBenefitAdjustment(birthYM, startYM)

    override fun determineNext(prevYear: YearlyDetail?): SSBenefitRec {
        val year = (prevYear?.year?.let { it + 1 } ?: currentDate.year)
        val cmpInflation = getCmpdInflationEnd(prevYear)

        val prevRec = prevYear?.benefits?.find { it.ident == ident }
        val targetStart = targetDate(prevRec, prevYear)
        val hasClaimed = targetStart.year < year || prevRec?.hasClaimed() ?: false
        val newClaim = !hasClaimed && targetStart.year == year

        val benefitAdj =
            if (!newClaim) prevRec?.benefitAdjustment ?: initialAdjustment()
            else calcBenefitAdjustment(birthYM, targetStart)

        val pctInYear =
            if (!newClaim) 1.0
            else 1 - targetStart.monthFraction()

        val baseAmount = baseAmount(prevRec, prevYear)
        val value = benefitAdj * baseAmount * cmpInflation * pctInYear

        return SSBenefitRec(
            year = year,
            ident = ident,
            amount = value,
            taxableAmount = taxabilityProfile.calcTaxable(ident.person, value),
            baseAmount = baseAmount,
            benefitAdjustment = benefitAdj
        )
    }
}
