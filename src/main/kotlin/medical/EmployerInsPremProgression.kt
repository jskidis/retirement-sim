package medical

import Amount
import YearlyDetail
import config.EmploymentConfig
import config.IEmployerInsurance
import inflation.CmpdInflationProvider
import inflation.MedCmpdInflationProvider

class EmployerInsPremProgression(
    val employments: List<EmploymentConfig>,
    val relation: RelationToInsured,
    val fullyDeduct: Boolean = true,
) : MedInsuranceProgression,
    CmpdInflationProvider by MedCmpdInflationProvider() {

    override fun determineNext(currYear: YearlyDetail, previousAGI: Amount): InsurancePrem {

        val currEmployers = employments.map {
            if (it.employerInsurance == null ||
                it.dateRange.pctInYear(currYear.year).value == 0.0
            ) null
            else Pair(it.employerInsurance, it.dateRange)
        }.filterNotNull()

        val currCobra = employments.map {
            if (it.employerInsurance?.cobraConfig == null) null
            else {
                val dateRange = it.employerInsurance.cobraConfig.effectiveDates(it)
                if (dateRange.pctInYear(currYear.year).value == 0.0) null
                else Pair(it.employerInsurance.cobraConfig, dateRange)
            }
        }.filterNotNull()

        val all = currEmployers + currCobra
        return all.fold(InsurancePrem(DESCRIPTION)) { acc, it ->
            if (acc.monthsCovered >= 12) acc
            else {
                val monthsCovered = Math.round(
                    it.second.pctInYear(currYear.year).value * 12.0
                ).toInt()
                val monthsNeedingCover = Math.min(12 - acc.monthsCovered, monthsCovered)

                val prem = premBasedOnRelation(it.first, relation) *
                    (monthsNeedingCover / 12.0) * getCmpdInflationStart(currYear)

                InsurancePrem(
                    name = acc.name,
                    premium = acc.premium + prem,
                    monthsCovered = acc.monthsCovered + monthsNeedingCover,
                    fullyDeductAmount = acc.fullyDeductAmount -
                        if (fullyDeduct) prem else 0.0
                )
            }
        }
    }

    companion object {
        const val DESCRIPTION = "MedIns-EmpProv"
    }

    private fun premBasedOnRelation(empInsurance: IEmployerInsurance, relation: RelationToInsured)
        : Double = when (relation) {
        RelationToInsured.SELF -> empInsurance.selfCost
        RelationToInsured.SPOUSE -> empInsurance.spouseCost
        RelationToInsured.DEPENDANT -> empInsurance.dependentCost
    }
}