package inflation

import Rate
import YearlyDetail

interface CmpdInflationProvider {
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
    override fun getRAC(inflationRec: InflationRec): InflationRAC = inflationRec.std
}

class MedCmpdInflationProvider : BaseCmpdInflationProvider {
    override fun getRAC(inflationRec: InflationRec): InflationRAC = inflationRec.med
}

class ChainCmpdInflationProvider : BaseCmpdInflationProvider {
    override fun getRAC(inflationRec: InflationRec): InflationRAC = inflationRec.chain
}

class WageCmpdInflationProvider : BaseCmpdInflationProvider {
    override fun getRAC(inflationRec: InflationRec): InflationRAC = inflationRec.wage
}

