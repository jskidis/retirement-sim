package medical

import Amount
import YearMonth
import YearlyDetail

open class MedicareProgression(val birthYM: YearMonth, val parts: List<MedicarePartType>)
    : MedInsuranceProgression, MedicarePremProvider by MedicarePremCalc()
{
    override fun determineNext(currYear: YearlyDetail, previousAGI: Amount): InsurancePrem {
        val monthsCovered = determineMonthsCovered(currYear)
        return if (monthsCovered == 0) InsurancePrem(name = DESCRIPTION)
        else InsurancePrem(
            name = DESCRIPTION,
            premium = getMedicarePremium(currYear, previousAGI, parts) * (monthsCovered / 12.0),
            monthsCovered = monthsCovered
        )
    }

    fun determineMonthsCovered(currYear: YearlyDetail): Int = when {
        currYear.year - birthYM.year < 65 -> 0
        currYear.year - birthYM.year > 65 -> 12
        else -> 11 - birthYM.month
    }

    companion object {
        const val DESCRIPTION = "Medicare"
    }
}
