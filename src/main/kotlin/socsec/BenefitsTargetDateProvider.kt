package socsec

import YearMonth
import YearlyDetail

fun interface BenefitsTargetDateProvider {
    fun targetDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): YearMonth
}

class StdBenefitsTargetDateProvider(val targetYM: YearMonth) : BenefitsTargetDateProvider {
    override fun targetDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): YearMonth = targetYM
}