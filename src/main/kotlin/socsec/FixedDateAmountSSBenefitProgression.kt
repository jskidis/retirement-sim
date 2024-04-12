package socsec

import Amount
import RecIdentifier
import YearMonth
import YearlyDetail
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import tax.TaxabilityProfile
import util.currentDate

open class FixedDateAmountSSBenefitProgression(
    val ident: RecIdentifier,
    val birthYM: YearMonth,
    val targetYM: YearMonth,
    val baseAmount: Amount,
    val taxabilityProfile: TaxabilityProfile,
    val benefitAdjustmentCalc: IBenefitAdjustmentCalc = BenefitAdjustmentCalc,
) : SSBenefitProgression,
    CmpdInflationProvider by StdCmpdInflationProvider() {

    var benefitAdjustment: Double = 0.0

    override fun determineNext(prevYear: YearlyDetail?): SSBenefitRec {
        val year = (prevYear?.year?.let { it + 1 } ?: currentDate.year)
        val cmpInflation = getCmpdInflationEnd(prevYear)

        if (benefitAdjustment == 0.0 && targetYM.year <= year)
            benefitAdjustment = benefitAdjustmentCalc.calcBenefitAdjustment(birthYM, targetYM)

        val pctInYear =
            if (year != targetYM.year) 1.0
            else 1 - targetYM.monthFraction()

        val value =
            if (benefitAdjustment == 0.0) 0.0
            else benefitAdjustment * baseAmount * cmpInflation * pctInYear

        return SSBenefitRec(
            year = year,
            ident = ident,
            amount = value,
            taxableAmount = taxabilityProfile.calcTaxable(ident.person, value)
        )
    }
}