package inflation

import Rate
import YearlyDetail

interface CmpdInflationProvider {
    fun getInflationType(): INFL_TYPE
    fun getCmpdInflationStart(currYear: YearlyDetail): Rate
    fun getCmpdInflationEnd(prevYear: YearlyDetail?): Rate
    fun getCurrInflationRate(year: YearlyDetail): Rate
}

interface BaseCmpdInflationProvider : CmpdInflationProvider {
    fun getRAC(inflationRec: InflationRec): InflationRAC

    override fun getCmpdInflationStart(currYear: YearlyDetail): Rate =
        getRAC(currYear.inflation).cmpdStart

    override fun getCmpdInflationEnd(prevYear: YearlyDetail?): Rate =
        prevYear?.let { getRAC(it.inflation).cmpdEnd } ?: 1.0

    override fun getCurrInflationRate(year: YearlyDetail): Rate = getRAC(year.inflation).rate
}

class StdCmpdInflationProvider : BaseCmpdInflationProvider {
    override fun getInflationType(): INFL_TYPE = INFL_TYPE.STD
    override fun getRAC(inflationRec: InflationRec): InflationRAC = inflationRec.std
}

class MedCmpdInflationProvider : BaseCmpdInflationProvider {
    override fun getInflationType(): INFL_TYPE = INFL_TYPE.MED
    override fun getRAC(inflationRec: InflationRec): InflationRAC = inflationRec.med
}

class WageCmpdInflationProvider : BaseCmpdInflationProvider {
    override fun getInflationType(): INFL_TYPE = INFL_TYPE.WAGE
    override fun getRAC(inflationRec: InflationRec): InflationRAC = inflationRec.wage
}

class HousingCmpdInflationProvider: BaseCmpdInflationProvider {
    override fun getInflationType(): INFL_TYPE = INFL_TYPE.HOUSING
    override fun getRAC(inflationRec: InflationRec): InflationRAC = inflationRec.housing
}

