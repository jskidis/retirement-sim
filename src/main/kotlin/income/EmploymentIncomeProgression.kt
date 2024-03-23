package income

import config.EmploymentConfig
import progression.AmountAdjusterWithGapFiller
import progression.DateRangeAmountAdjuster

open class EmploymentIncomeProgression(
    employmentConfig: EmploymentConfig,
    adjusters: List<AmountAdjusterWithGapFiller>
) : BasicIncomeProgression(
    startAmount = employmentConfig.startSalary,
    config = EmploymentConfig.incomeConfig(employmentConfig),
    adjusters = listOf(DateRangeAmountAdjuster(employmentConfig.dateRange)) + adjusters
)
