package medical

import Amount
import Year
import YearlyDetail

class DependantInsFixedYearProgression(val startYear: Year) : MedInsuranceProgression {
    override fun determineNext(currYear: YearlyDetail, previousAGI: Amount): InsurancePrem =
        if (currYear.year >= startYear) InsurancePrem(name = DESCRIPTION, monthsCovered = 12)
        else InsurancePrem(name = DESCRIPTION)

    companion object {
        const val DESCRIPTION = "MedIns-DependProv"
    }
}