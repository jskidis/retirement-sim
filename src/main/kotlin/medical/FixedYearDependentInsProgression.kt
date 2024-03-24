package medical

import Year
import YearlyDetail
import progression.CYProgression

class FixedYearDependentInsProgression(val startYear: Year) : CYProgression<InsurancePrem> {
    override fun determineNext(currYear: YearlyDetail): InsurancePrem =
        if (currYear.year >= startYear) InsurancePrem(0.0, 12)
        else InsurancePrem(0.0, 0)
}