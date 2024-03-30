package medical

import Amount
import YearlyDetail
import config.EmployerInsurance
import config.EmploymentConfig

class EmployerInsPremProgression(
    val employments: List<EmploymentConfig>,
    val relation: RelationToInsured,
    val fullyDeduct: Boolean = true
) : MedInsuranceProgression {

    override fun determineNext(currYear: YearlyDetail, previousAGI: Amount): InsurancePrem {
        val currEmployers = employments.filter {
            it.employerInsurance != null &&
                it.dateRange.pctInYear(currYear.year).value > 0.0
        }

        return currEmployers.fold(InsurancePrem(DESCRIPTION)) { acc, it ->
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
                    name = acc.name,
                    premium = acc.premium + prem,
                    monthsCovered = acc.monthsCovered + monthsNeedingCover,
                    fullyDeductAmount = acc.fullyDeductAmount +
                        if (fullyDeduct) prem else 0.0
                )
            }
        }
    }

    companion object {
        const val DESCRIPTION = "MedIns-EmpProv"
    }

    private fun premBasedOnRelation(empInsurance: EmployerInsurance, relation: RelationToInsured)
        : Double = when (relation) {
        RelationToInsured.SELF -> empInsurance.selfCost
        RelationToInsured.SPOUSE -> empInsurance.spouseCost
        RelationToInsured.DEPENDANT -> empInsurance.dependantCost
    }
}