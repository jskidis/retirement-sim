package medical

import Amount
import YearlyDetail
import inflation.CmpdInflationProvider
import inflation.MedCmpdInflationProvider
import tax.FilingStatus

open class MedicarePremCalc : MedicarePremProvider,
    CmpdInflationProvider by MedCmpdInflationProvider() {
    override fun getMedicarePremium(
        currYear: YearlyDetail,
        previousAGI: Amount,
        parts: List<MedicarePartType>,
    ): Double {
        val adjustedAgi = previousAGI / currYear.inflation.std.cmpdStart
        val brackets = getBrackets()
        val bracket =
            if (FilingStatus.JOINTLY == currYear.filingStatus) brackets.find {
                it.jointBracket.start <= adjustedAgi &&
                    it.jointBracket.end >= adjustedAgi
            }
            else brackets.find {
                it.singleBracket.start <= adjustedAgi &&
                    it.singleBracket.end >= adjustedAgi

            }
        val prems = bracket?.partPrems
            ?: throw RuntimeException("Unable to find medicare premium for given AGI")

        return parts.fold(0.0) { acc, it ->
            acc + it.getPartPrem(prems)
        } * getCmpdInflationStart(currYear)
    }

    open fun getBrackets(): List<MedicarePremBracketRec> = MedicarePremiumBrackets.brackets
}

interface MedicarePremProvider {
    fun getMedicarePremium(
        currYear: YearlyDetail,
        previousAGI: Amount,
        parts: List<MedicarePartType>,
    ): Double
}

