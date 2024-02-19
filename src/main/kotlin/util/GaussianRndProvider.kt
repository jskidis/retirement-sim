package util

import kotlin.random.Random
import kotlin.random.asJavaRandom

interface GaussianRndProvider {
    fun gaussianRndValue(): Double = Random.asJavaRandom().nextGaussian()
}