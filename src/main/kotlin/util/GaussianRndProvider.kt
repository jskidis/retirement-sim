package util

import YearlyDetail
import kotlin.random.Random
import kotlin.random.asJavaRandom

interface GaussianRndProvider {
    fun gaussianRndValue(): Double = Random.asJavaRandom().nextGaussian()
}

interface GaussianRndFromPrevYear {
    fun gaussianRndValue(prevYear: YearlyDetail?): Double = prevYear?.rorRndGaussian ?: 0.0
}