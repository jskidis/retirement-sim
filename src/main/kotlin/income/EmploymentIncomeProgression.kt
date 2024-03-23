package income

import YearlyDetail
import config.EmploymentConfig
import progression.AmountAdjusterWithGapFiller
import progression.DateRangeAmountAdjuster

open class EmploymentIncomeProgression(
    val employmentConfig: EmploymentConfig,
    adjusters: List<AmountAdjusterWithGapFiller>,
) : BasicIncomeProgression(
    startAmount = employmentConfig.startSalary,
    config = EmploymentConfig.incomeConfig(employmentConfig),
    adjusters = listOf(DateRangeAmountAdjuster(employmentConfig.dateRange)) + adjusters
) {
    override fun determineNext(prevYear: YearlyDetail?): IncomeRec {
        val income = super.determineNext(prevYear)
        val bonus = employmentConfig.bonusCalc?.calcBonus(income.baseAmount, prevYear) ?: 0.0

        return if (bonus <= 0.0) income
        else income.copy(bonus = bonus)
    }
}
