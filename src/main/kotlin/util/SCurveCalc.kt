package util

object SCurveCalc {
    fun calcValue(
        index: Double,
        indexRange: Pair<Double, Double>,
        valueRange: Pair<Double, Double>,
    ): Double =
        if (index < indexRange.first) valueRange.first
        else if (index > indexRange.second) valueRange.second
        else {
            val pctInRange =
                ((indexRange.second - indexRange.first) - (index - indexRange.first)) /
                    (indexRange.second - indexRange.first)

            val curvePct =
                if (pctInRange > .5) Math.sin(Math.PI * (pctInRange - .5)) / 2.0 + 0.5
                else 0.5 - (Math.cos(Math.PI * pctInRange) / 2.0)

            (valueRange.first - valueRange.second) * curvePct + valueRange.second
        }
}