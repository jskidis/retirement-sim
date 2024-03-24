package medical

import Amount

data class InsurancePrem(
    val premium: Amount = 0.0,
    val monthsCovered: Int = 0,
    val fullyDeduct: Boolean = false
) {
    fun hasCoverage(): Boolean = monthsCovered > 0
}
