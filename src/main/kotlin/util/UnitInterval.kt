package util

data class UnitInterval (
    val d: Double,
    val value: Double = when {
        d < 0 -> 0.0
        d > 1 -> 1.0
        else -> d
    }
)