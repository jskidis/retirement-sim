package socsec

import Rate
import YearMonth

fun interface BenefitAdjustmentCalc {
    fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Rate
}

object StdBenefitAdjustmentCalc : BenefitAdjustmentCalc {
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

object SpousalBenefitAdjustmentCalc: BenefitAdjustmentCalc {
    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Rate {
        val ageAtStart = startYM.toDec() - birthYM.toDec()
        val fullRetirementAge = if (birthYM.year >= 1955) 67.0 else 66.0
        val yearsFromFRA = fullRetirementAge - ageAtStart
        return adjustment(yearsFromFRA)
    }

    private fun adjustment(yearsFromFRA: Double): Double = when {
        yearsFromFRA > 5.0 -> 0.0
        yearsFromFRA > 3.0 -> 0.375 - ((yearsFromFRA-3) /40)
        yearsFromFRA > 0.0 -> 0.5 - (yearsFromFRA /24)
        else -> 0.5
    }
}

object SpousalSurvivorBenefitAdjustmentCalc: BenefitAdjustmentCalc {
    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Rate {
        val ageAtStart = startYM.toDec() - birthYM.toDec()
        val fullRetirementAge = if (birthYM.year >= 1955) 67.0 else 66.0
        val yearsFromFRA = fullRetirementAge - ageAtStart
        return adjustment(yearsFromFRA)
    }

    private fun adjustment(yearsFromFRA: Double): Double = when {
        yearsFromFRA > 17.0 -> 0.0
        yearsFromFRA > 7.0 -> 0.715 - ((yearsFromFRA-7) /80)
        yearsFromFRA > 0.0 -> 1.0 - (yearsFromFRA /24)
        else -> 1.0
    }
}

object DisabledChildSurvivorBenefitsAdjustmentCalc: BenefitAdjustmentCalc {
    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Rate  = 0.75
}

object DisabledPersonBenefitAdjustmentCalc : BenefitAdjustmentCalc {
    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Rate  = 1.00
}