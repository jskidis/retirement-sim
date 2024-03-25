package medical

import Amount

data class InsurancePrem(
    val name: String,
    val premium: Amount = 0.0,
    val monthsCovered: Int = 0,
    val fullyDeductAmount: Amount = 0.0
) {
    fun hasCoverage(): Boolean = monthsCovered > 0
}
