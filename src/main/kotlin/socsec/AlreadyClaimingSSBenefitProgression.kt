package socsec

import Amount
import Rate
import YearMonth
import YearlyDetail
import config.Person

open class AlreadyClaimingSSBenefitProgression(
    person: Person,
    val claimDate: YearMonth,
    currentAmount: Amount,
    benefitAdjustmentCalc: BenefitAdjustmentCalc = StdBenefitAdjustmentCalc,
) : BenefitBaseAmountProvider, BenefitsClaimDateProvider, DefaultAdjustmentProvider {

    val adjustment = benefitAdjustmentCalc.calcBenefitAdjustment(person.birthYM, claimDate)
    val baseAmount = currentAmount / adjustment

    override fun baseAmount(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): Amount = baseAmount
    override fun claimDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): YearMonth = claimDate
    override fun initialAdjustment(): Rate = adjustment
}
