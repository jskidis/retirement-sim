package socsec

import Amount
import YearMonth
import YearlyDetail
import util.currentDate

open class FixedDateAmountSSBenefitProgression(
    val config: SSBenefitConfig,
    val birthYM: YearMonth,
    val targetYM: YearMonth,
    val baseAmount: Amount,
    val benefitAdjustmentF: (YearMonth, YearMonth) -> Double = BenefitAdjustmentCalc::calcBenefitAdjustment,
) : SSBenefitProgression {

    var benefitAdjustment: Double = 0.0

    override fun determineNext(prevYear: YearlyDetail?): SSBenefitRec {
        val year = (prevYear?.year?.let { it + 1 } ?: currentDate.year)
        val cmpInflation = (prevYear?.inflation?.std?.cmpdEnd) ?: 1.0

        if (benefitAdjustment == 0.0 && targetYM.year <= year)
            benefitAdjustment = benefitAdjustmentF(birthYM, targetYM)

        val pctInYear =
            if (year != targetYM.year) 1.0
            else 1 - targetYM.monthFraction()

        val value =
            if (benefitAdjustment == 0.0) 0.0
            else benefitAdjustment * baseAmount * cmpInflation * pctInYear

        return SSBenefitRec(
            year = year,
            config = config,
            amount = value,
            taxableAmount = config.taxabilityProfile.calcTaxable(config.person, value)
        )
    }
}