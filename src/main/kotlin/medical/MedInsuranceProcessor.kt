package medical

import Amount
import Name
import Year
import YearlyDetail
import config.SimConfig
import expense.ExpenseConfig
import expense.ExpenseRec
import tax.TaxableAmounts

object MedInsuranceProcessor {
    fun process(config: SimConfig, currYear: YearlyDetail): List<ExpenseRec> {
        return config.household.members.people().flatMap { person ->
            val prems = person.medInsurance().map { medIns ->
                medIns.determineNext(currYear)
            }.filter { it.hasCoverage() }


            whichToUse(prems).map { prem ->
                createExpenseRec(currYear.year, prem.name, person.name(),
                    prem.premium, prem.fullyDeductAmount)
            }
        }
    }

    private fun whichToUse(prems: List<InsurancePrem>): List<InsurancePrem> {
        var monthsCovered = 0
        val insuranceUsed: MutableList<InsurancePrem> = ArrayList()

        prems.forEach {
            if (monthsCovered < 12) {
                val monthsUsed = Math.min(12 - monthsCovered, it.monthsCovered)
                monthsCovered += monthsUsed
                insuranceUsed.add(it.prorate(monthsUsed))
            }
        }
        return insuranceUsed
    }

    private fun createExpenseRec(
        year: Year, name: Name, person: Name,
        premium: Amount, deductAmount: Amount,
    ) =
        ExpenseRec(
            year = year,
            config = ExpenseConfig(
                name = name,
                person = person
            ),
            amount = premium,
            taxDeductions = TaxableAmounts(
                person = person,
                fed = deductAmount,
                state = deductAmount
            )
        )
}
