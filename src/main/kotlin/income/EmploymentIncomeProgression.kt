package income

import YearlyDetail
import config.EmploymentConfig
import progression.AmountAdjusterWithGapFiller
import progression.DateRangeAmountAdjuster

open class EmploymentIncomeProgression(
    val employmentConfig: EmploymentConfig,
    adjusters: List<AmountAdjusterWithGapFiller>,
) : BasicIncomeProgression(
    ident = employmentConfig.ident,
    startAmount = employmentConfig.startSalary,
    taxabilityProfile = employmentConfig.taxabilityProfile,
    adjusters = listOf(DateRangeAmountAdjuster(employmentConfig.dateRange)) + adjusters
) {
    override fun determineNext(prevYear: YearlyDetail?): IncomeRec {
        val income = super.determineNext(prevYear)
        val bonus = employmentConfig.bonusCalc?.calcBonus(income.baseAmount, prevYear) ?: 0.0

        return if (bonus <= 0.0) income
        else income.copy(bonus = bonus,
            taxableIncome = taxabilityProfile.calcTaxable(
                ident.name, amount = income.baseAmount + bonus)
        )
    }
}
