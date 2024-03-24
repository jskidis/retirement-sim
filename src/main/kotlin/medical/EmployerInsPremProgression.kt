package medical

import YearlyDetail
import config.EmployerInsurance
import config.EmploymentConfig
import progression.CYProgression

enum class RelationToInsured {
    SELF, SPOUSE, DEPENDANT
}

class EmployerInsPremProgression(
    val employments: List<EmploymentConfig>,
    val relation: RelationToInsured,
    val fullyDeduct: Boolean = true
) : CYProgression<InsurancePrem> {

    override fun determineNext(currYear: YearlyDetail): InsurancePrem {
        val currEmployers = employments.filter {
            it.employerInsurance != null &&
                it.dateRange.pctInYear(currYear.year).value > 0.0
        }

        return currEmployers.fold(InsurancePrem()) { acc, it ->
            if (acc.monthsCovered >= 12 || it.employerInsurance == null) acc
            else {
                val monthsCovered = Math.round(
                    it.dateRange.pctInYear(currYear.year).value * 12.0
                ).toInt()
                val monthsNeedingCover = Math.min(12 - acc.monthsCovered, monthsCovered)

                val prem = premBasedOnRelation(it.employerInsurance, relation) *
                    (monthsNeedingCover / 12.0) *
                    currYear.inflation.med.cmpdStart

                InsurancePrem(
                    premium = acc.premium + prem,
                    monthsCovered = acc.monthsCovered + monthsNeedingCover,
                    fullyDeduct = fullyDeduct)
            }
        }
    }

    private fun premBasedOnRelation(empInsurance: EmployerInsurance, relation: RelationToInsured)
        : Double = when (relation) {
        RelationToInsured.SELF -> empInsurance.selfCost
        RelationToInsured.SPOUSE -> empInsurance.spouseCost
        RelationToInsured.DEPENDANT -> empInsurance.dependantCost
    }
}