package income

import YearlyDetail
import config.SimConfig
import inflation.CmpdInflationProvider
import inflation.WageCmpdInflationProvider
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.SS_INCOME_CAP

object IncomeProcessor : CmpdInflationProvider by WageCmpdInflationProvider() {
    fun process(config: SimConfig, prevYear: YearlyDetail?): List<IncomeRec> =
        config.incomeConfigs(prevYear).map { income ->
            capSocSecTaxableIncome(
                income.determineNext(prevYear), prevYear)
        }.filter { it.retainRec() }

    private fun capSocSecTaxableIncome(incomeRec: IncomeRec, prevYear: YearlyDetail?): IncomeRec {
        val cap = ConstantsProvider.getValue(SS_INCOME_CAP) * getCmpdInflationEnd(prevYear)
        val roundedCap = (Math.round(cap / 100.0)) * 100.0
        return if (incomeRec.taxable().socSec <= roundedCap) incomeRec
        else incomeRec.updateTaxable(incomeRec.taxable().copy(socSec = roundedCap))
    }
}