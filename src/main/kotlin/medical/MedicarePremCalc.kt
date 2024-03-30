package medical

import Amount
import YearlyDetail
import tax.FilingStatus

open class MedicarePremCalc : MedicarePremProvider {
    override fun getMedicarePremium(currYear: YearlyDetail, previousAGI: Amount): Double {
        val adjustedAgi = previousAGI / currYear.inflation.std.cmpdStart
        val brackets = getBrackets()
        val unadjustedPrems =
            if (FilingStatus.JOINTLY == currYear.filingStatus) brackets.find {
                it.jointBracket.start <= adjustedAgi &&
                    it.jointBracket.end >= adjustedAgi
            }
            else brackets.find {
                it.singleBracket.start <= adjustedAgi &&
                    it.singleBracket.end >= adjustedAgi

            }
        val prems = unadjustedPrems?.let {
            MedicarePartPrems(
                partBPrem = it.partPrems.partBPrem * currYear.inflation.med.cmpdStart,
                partDPrem = it.partPrems.partDPrem * currYear.inflation.med.cmpdStart,
            )
        } ?: throw RuntimeException("Unable to find medicare premium for given AGI")
        return prems.partBPrem + prems.partDPrem
    }

    open fun getBrackets(): List<MedicarePremBracketRec> = MedicarePremiumBrackets.brackets
}

interface MedicarePremProvider {
    fun getMedicarePremium(currYear: YearlyDetail, previousAGI: Amount): Double
}

