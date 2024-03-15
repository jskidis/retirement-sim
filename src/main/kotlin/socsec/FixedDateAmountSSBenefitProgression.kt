package socsec

import Amount
import YearMonth
import YearlyDetail
import progression.Progression
import socsec.BenefitAdjustmentCalc.calcBenefitAdjustment
import util.currentDate

class FixedDateAmountSSBenefitProgression(
    val config:SSBenefitConfig,
    val birthYM: YearMonth,
    val targetYM: YearMonth,
    val baseAmount: Amount,
) : Progression<SSBenefitRec> {

    var benefitAdjustment: Double = 0.0

    override fun determineNext(prevYear: YearlyDetail?): SSBenefitRec {
        val year = (prevYear?.year?.let { it + 1 } ?: currentDate.year) + 1
        val cmpInflation = (prevYear?.inflation?.std?.cmpdEnd) ?: 1.0

        if (benefitAdjustment == 0.0 && targetYM.year <= year)
            benefitAdjustment = calcBenefitAdjustment(birthYM, targetYM)

        val value = benefitAdjustment * baseAmount * cmpInflation
        return SSBenefitRec(
            year = year,
            config = config,
            amount = value,
            taxableAmount = config.taxabilityProfile.calcTaxable(config.person, value)
        )
    }
}