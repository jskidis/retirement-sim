package socsec

import Amount
import YearlyDetail

fun interface BenefitBaseAmountProvider {
    fun baseAmount(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): Amount
}

class StdBenefitBaseAmountProvider(val baseAmount: Amount) : BenefitBaseAmountProvider {
    override fun baseAmount(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): Amount = baseAmount
}