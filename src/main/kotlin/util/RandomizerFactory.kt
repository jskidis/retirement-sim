package util

import Name
import YearlyDetail
import config.SimConfig
import kotlin.random.Random
import kotlin.random.asJavaRandom

object RandomizerFactory {

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

    fun suppressRandom(): Boolean =
        System.getProperty("suppressRand")?.let{it.toBooleanStrictOrNull() ?: false} ?: false

    fun getROIRandom(prevYear: YearlyDetail?): Double =
        getRandomValue(GaussKeys.ROI.name, prevYear)

    fun getInflRandom(prevYear: YearlyDetail?): Double =
        getRandomValue(GaussKeys.INFLATION.name, prevYear)

    fun getPersonRandom(person: Name, prevYear: YearlyDetail?): Double =
        getRandomValue(person, prevYear)
}
