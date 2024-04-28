package income

import Amount
import YearlyDetail
import config.EmploymentConfig
import progression.AmountAdjusterWithGapFiller
import progression.DateRangeAmountAdjuster
import util.RecFinder
import util.yearFromPrevYearDetail

open class EmploymentIncomeProgression(
    val employmentConfig: EmploymentConfig,
    adjusters: List<AmountAdjusterWithGapFiller>,
) : BasicIncomeProgression(
    ident = employmentConfig.ident,
    startAmount = employmentConfig.startSalary,
    taxabilityProfile = employmentConfig.taxabilityProfile,
    adjusters = listOf(DateRangeAmountAdjuster(employmentConfig.dateRange)) + adjusters
) {
    override fun previousAmount(prevYear: YearlyDetail): Amount? {
        val prevRec = RecFinder.findIncomeRec(ident, prevYear)
        return (prevRec as? IncomeWithBonusRec)?.baseAmount ?: prevRec?.amount()
    }

    override fun createRecord(value: Amount, prevYear: YearlyDetail?): IncomeRec {
        val bonus = Math.max(0.0,
            employmentConfig.bonusCalc?.calcBonus(value, prevYear) ?: 0.0)

        return IncomeWithBonusRec(
            year = yearFromPrevYearDetail(prevYear),
            ident = ident,
            baseAmount = value,
            bonus = bonus,
            taxableIncome = taxabilityProfile.calcTaxable(ident.person, value + bonus))
    }
}
