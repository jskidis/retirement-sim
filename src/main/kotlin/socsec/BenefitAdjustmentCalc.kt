package socsec

import Rate
import YearMonth

fun interface IBenefitAdjustmentCalc {
    fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Rate
}


object BenefitAdjustmentCalc : IBenefitAdjustmentCalc {
    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Rate {
        val ageAtStart = startYM.toDec() - birthYM.toDec()

        return if (birthYM.year >= 1955) post1955(ageAtStart)
        else pre1955(ageAtStart)
    }

    private fun post1955(ageAtStart: Double): Double = when {
        ageAtStart < 62.0 -> 0.0
        ageAtStart < 64.0 -> .70 + (ageAtStart - 62.0) / 20.0
        ageAtStart < 67.0 -> .80 + (ageAtStart - 64.0) / 15.0
        ageAtStart < 70.0 -> 1.0 + (ageAtStart - 67.0) / 12.5
        else -> 1.24
    }

    private fun pre1955(ageAtStart: Double): Double = when {
        ageAtStart < 62.0 -> 0.0
        ageAtStart < 63.0 -> .75 + (ageAtStart - 62.0) / 20.0
        ageAtStart < 66.0 -> .80 + (ageAtStart - 63.0) / 15.0
        ageAtStart < 70.0 -> 1.0 + (ageAtStart - 66.0) / 12.5
        else -> 1.32
    }
}