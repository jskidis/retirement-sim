package medical

import YearMonth
import YearlyDetail
import config.ConfigConstants
import progression.CYProgression

class MedicareProgression(val birthYM: YearMonth) : CYProgression<InsurancePrem> {
    override fun determineNext(currYear: YearlyDetail): InsurancePrem {
        val monthsCovered = determineMonthsCovered(currYear)
        return if (monthsCovered == 0) InsurancePrem()
        else InsurancePrem(
            premium = getBasePremium(currYear) * (monthsCovered / 12.0),
            monthsCovered = monthsCovered
        )
    }

    fun getBasePremium(currYear: YearlyDetail): Double {
        return ConfigConstants.baseMedicarePrem * currYear.inflation.med.cmpdStart
    }

    fun determineMonthsCovered(currYear: YearlyDetail): Int = when {
        currYear.year - birthYM.year < 65 -> 0
        currYear.year - birthYM.year > 65 -> 12
        else -> 11 - birthYM.month
    }
}