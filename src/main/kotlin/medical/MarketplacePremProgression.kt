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
    val ageFactorRetrieval: MPAgeFactorRetrieval = MPAgeMap,
    val medalPlanRetrieval: MPMedalPlanFactorRetrieval = MPMedalPlanMap,
    val cmpdInflationProver: CmpdInflationProvider = MedCmpdInflationProvider()
) : MedInsuranceProgression
{
    override fun determineNext(currYear: YearlyDetail, previousAGI: Amount): InsurancePrem {
        val premium = (ConstantsProvider.getValue(MARKETPLACE_BASE_PREM) +
            if(includeDental) ConstantsProvider.getValue(DENTAL_BASE_PREM) else 0.0) *
            cmpdInflationProver.getCmpdInflationStart(currYear) *
            ageFactorRetrieval.getAgeFactor(currYear.year - birthYM.year) *
            medalPlanRetrieval.getMedalPlanFactor(medalType, planType)

        return InsurancePrem(
            name = DESCRIPTION, premium = premium, monthsCovered = 12)
    }

    companion object {
        const val DESCRIPTION = "MedIns-Marketplace"
    }
}

fun interface MPAgeFactorRetrieval {
    fun getAgeFactor(age: Int): Double
}

fun interface MPMedalPlanFactorRetrieval {
    fun getMedalPlanFactor(medal: MPMedalType, plan: MPPlanType): Double
}

data class MedalPlanFactor(
    val medal: MPMedalType,
    val plan: MPPlanType,
    val factor: Double,
)

object MPAgeMap : MPAgeFactorRetrieval {
    val ageMap: YearBasedConfig<Double> by lazy {
        YearBasedConfig(list = loadMap())
    }

    override fun getAgeFactor(age: Int): Double = ageMap.getConfigForYear(age)

    private fun loadMap(): List<YearConfigPair<Double>> =
        getReader().readCsvFromResource("tables/mp-prem-age.csv")


    private fun getReader() = CSVReader { it: CSVRecord ->
        YearConfigPair(startYear = it[0].toInt(), config = it[1].toDouble())
    }
}

object MPMedalPlanMap : MPMedalPlanFactorRetrieval {
    val planMap: List<MedalPlanFactor> by lazy {
        loadMap()
    }

    override fun getMedalPlanFactor(medal: MPMedalType, plan: MPPlanType): Double =
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