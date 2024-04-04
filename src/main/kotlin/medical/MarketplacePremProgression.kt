package medical

import Amount
import YearMonth
import YearlyDetail
import inflation.CmpdInflationProvider
import inflation.MedCmpdInflationProvider
import org.apache.commons.csv.CSVRecord
import util.CSVReader
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.DENTAL_BASE_PREM
import util.ConstantsProvider.KEYS.MARKETPLACE_BASE_PREM
import util.YearBasedConfig
import util.YearConfigPair

open class MarketplacePremProgression(
    val birthYM: YearMonth,
    val medalType: MPMedalType,
    val planType: MPPlanType,
    val includeDental: Boolean = false,
) : MedInsuranceProgression,
    MPAgeFactorRetrieval, MPMealPlanFactorRetrieval,
    CmpdInflationProvider by MedCmpdInflationProvider()
{
    override fun determineNext(currYear: YearlyDetail, previousAGI: Amount): InsurancePrem {
        val premium = (ConstantsProvider.getValue(MARKETPLACE_BASE_PREM) +
            if(includeDental) ConstantsProvider.getValue(DENTAL_BASE_PREM) else 0.0) *
            getCmpdInflationStart(currYear) *
            getAgeFactor(currYear.year - birthYM.year) *
            getMedalPlanFactor(medalType, planType)

        return InsurancePrem(
            name = DESCRIPTION, premium = premium, monthsCovered = 12)
    }

    override fun getAgeFactor(age: Int): Double =
        MPAgeMap.getAgeFactor(age)

    override fun getMedalPlanFactor(medal: MPMedalType, plan: MPPlanType): Double =
        MPMedalPlanMap.getMedalPlanFactor(medal, plan)

    companion object {
        const val DESCRIPTION = "MedIns-Marketplace"
    }
}

interface MPAgeFactorRetrieval {
    fun getAgeFactor(age: Int): Double
}

interface MPMealPlanFactorRetrieval {
    fun getMedalPlanFactor(medal: MPMedalType, plan: MPPlanType): Double
}

data class MedalPlanFactor(
    val medal: MPMedalType,
    val plan: MPPlanType,
    val factor: Double,
)

object MPAgeMap {
    val ageMap: YearBasedConfig<Double> by lazy {
        YearBasedConfig(list = loadMap())
    }

    fun getAgeFactor(age: Int): Double = ageMap.getConfigForYear(age)

    private fun loadMap(): List<YearConfigPair<Double>> =
        getReader().readCsvFromResource("tables/mp-prem-age.csv")


    private fun getReader() = CSVReader { it: CSVRecord ->
        YearConfigPair(startYear = it[0].toInt(), config = it[1].toDouble())
    }
}

object MPMedalPlanMap {
    val planMap: List<MedalPlanFactor> by lazy {
        loadMap()
    }

    fun getMedalPlanFactor(medal: MPMedalType, plan: MPPlanType): Double =
        planMap.find { it.medal == medal && it.plan == plan }?.factor
            ?: throw RuntimeException("Unable to find premium for marketplace medal & plan")

    private fun loadMap(): List<MedalPlanFactor> =
        getReader().readCsvFromResource("tables/mp-prem-medal-plan.csv")

    private fun getReader() = CSVReader { it: CSVRecord ->
        MedalPlanFactor(
            medal = MPMedalType.valueOf(it[0]),
            plan = MPPlanType.valueOf(it[1]),
            factor = it[2].toDouble())
    }
}