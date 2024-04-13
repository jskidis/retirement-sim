package inflation

import Amount
import YearlyDetail
import progression.AmountAdjusterWithGapFiller

interface InflationAmountAdjuster : AmountAdjusterWithGapFiller, CmpdInflationProvider {

    override fun adjustAmount(value: Amount, prevYear: YearlyDetail): Amount =
        value * (1 + getCurrInflationRate(prevYear))

    override fun adjustGapFillValue(value: Amount, prevYear: YearlyDetail): Amount =
        value * getCmpdInflationEnd(prevYear)
}

class StdInflationAmountAdjuster : InflationAmountAdjuster,
    CmpdInflationProvider by StdCmpdInflationProvider()

class WageInflationAmountAdjust: InflationAmountAdjuster,
    CmpdInflationProvider by WageCmpdInflationProvider()
