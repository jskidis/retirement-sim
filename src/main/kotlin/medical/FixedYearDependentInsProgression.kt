package medical

import Year
import YearlyDetail
import progression.CYProgression

class FixedYearDependentInsProgression(val startYear: Year) : CYProgression<InsurancePrem> {
    override fun determineNext(currYear: YearlyDetail): InsurancePrem =
        if (currYear.year >= startYear) InsurancePrem(name = DESCRIPTION, monthsCovered = 12)
        else InsurancePrem(name = DESCRIPTION)

    companion object {
        const val DESCRIPTION = "DependentProvided"
    }
}