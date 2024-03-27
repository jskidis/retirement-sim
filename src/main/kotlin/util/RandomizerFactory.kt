package util

import Name
import YearlyDetail
import config.SimConfig
import kotlin.random.Random
import kotlin.random.asJavaRandom

object RandomizerFactory
    : RoiRandomProvider, InflRandomProvider, PersonRandomProvider {

    enum class GaussKeys { ROI, INFLATION }

    fun getRandomValue(key: String, prevYear: YearlyDetail?): Double =
        prevYear?.let { it.randomValues[key] ?: 0.0 } ?: 0.0

    fun createNewValues(config: SimConfig): Map<String, Double> =
        (GaussKeys.entries.map { it.name to getGaussRnd()  } +
            config.household.members.people().map { it.name() to getDoubleRnd() }).toMap()

    private fun getGaussRnd() =
        if(suppressRandom()) 0.0
        else Random.asJavaRandom().nextGaussian()

    private fun getDoubleRnd() =
        if(suppressRandom()) 0.0
        else Random.nextDouble()

    private fun suppressRandom(): Boolean =
        System.getProperty("suppressRand")?.let{it.toBooleanStrictOrNull() ?: false} ?: false

    override fun getRoiRandom(prevYear: YearlyDetail?): Double =
        getRandomValue(GaussKeys.ROI.name, prevYear)

    override fun getInflRandom(prevYear: YearlyDetail?): Double =
        getRandomValue(GaussKeys.INFLATION.name, prevYear)

    override fun getPersonRandom(person: Name, prevYear: YearlyDetail?): Double =
        getRandomValue(person, prevYear)
}

interface RoiRandomProvider {
    fun getRoiRandom(prevYear: YearlyDetail?): Double
}

interface InflRandomProvider {
    fun getInflRandom(prevYear: YearlyDetail?): Double
}

interface PersonRandomProvider {
    fun getPersonRandom(person: Name, prevYear: YearlyDetail?): Double
}