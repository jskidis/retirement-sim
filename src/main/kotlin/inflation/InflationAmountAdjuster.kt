package inflation

import Amount
import YearlyDetail
import progression.AmountAdjusterWithGapFiller

abstract class InflationAmountAdjuster : AmountAdjusterWithGapFiller {
    abstract fun getPrevInflationRAC(prevYear: YearlyDetail): InflationRAC

    override fun adjustAmount(value: Amount, prevYear: YearlyDetail): Amount =
        value * (1 + getPrevInflationRAC(prevYear).rate)

    override fun adjustGapFillValue(value: Amount, prevYear: YearlyDetail): Amount =
        value * getPrevInflationRAC(prevYear).cmpdEnd
}

class StdInflationAmountAdjuster : InflationAmountAdjuster() {
    override fun getPrevInflationRAC(prevYear: YearlyDetail): InflationRAC =
        prevYear.inflation.std
}

class MedInflationAmountAdjuster : InflationAmountAdjuster() {
    override fun getPrevInflationRAC(prevYear: YearlyDetail): InflationRAC =
        prevYear.inflation.med
}

class ChainInflationAmountAdjuster : InflationAmountAdjuster() {
    override fun getPrevInflationRAC(prevYear: YearlyDetail): InflationRAC =
        prevYear.inflation.chain
}

class WageInflationAmountAdjuster : InflationAmountAdjuster() {
    override fun getPrevInflationRAC(prevYear: YearlyDetail): InflationRAC =
        prevYear.inflation.wage
}
