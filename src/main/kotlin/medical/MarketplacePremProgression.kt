package medical

import YearMonth
import YearlyDetail
import config.ConfigConstants
import org.apache.commons.csv.CSVRecord
import progression.CYProgression
import util.CSVReader
import util.YearBasedConfig
import util.YearConfigPair

open class MarketplacePremProgression(
    val birthYM: YearMonth,
    val medalType: MPMedalType,
    val planType: MPPlanType,
) : CYProgression<InsurancePrem>,
    MPAgeFactorRetrieval, MPMealPlanFactorRetrieval
{
    override fun determineNext(currYear: YearlyDetail): InsurancePrem {
        val premium = ConfigConstants.marketPlaceBasePrem *
            currYear.inflation.med.cmpdStart *
            getAgeFactor(currYear.year - birthYM.year) *
            getMedalPlanFactor(medalType, planType)

        return InsurancePrem(premium =  premium, monthsCovered = 12, fullyDeduct = false)
    }

    override fun getAgeFactor(age: Int): Double =
        MPAgeMap.getAgeFactor(age)

    override fun getMedalPlanFactor(medal: MPMedalType, plan: MPPlanType): Double =
        MPMedalPlanMap.getMedalPlanFactor(medal, plan)
}

interface MPAgeFactorRetrieval {
    fun getAgeFactor(age: Int): Double
}

interface MPMealPlanFactorRetrieval {
    fun getMedalPlanFactor(medal: MPMedalType, plan: MPPlanType): Double
}

enum class MPMedalType {
    BRONZE, SILVER, GOLD, PLATINUM
}

enum class MPPlanType {
    HMO, EPO, PPO
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