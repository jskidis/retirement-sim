package medical

import Amount
import YearlyDetail

data class InsurancePrem(
    val name: String,
    val premium: Amount = 0.0,
    val monthsCovered: Int = 0,
    val fullyDeductAmount: Amount = 0.0,
) {
    fun hasCoverage(): Boolean = monthsCovered > 0
    fun prorate(months: Int): InsurancePrem =
        if (months >= monthsCovered) this
        else this.copy(
            premium = premium * months / monthsCovered,
            fullyDeductAmount = fullyDeductAmount * months / monthsCovered
        )
}

interface MedInsuranceProgression {
    fun determineNext(currYear: YearlyDetail, previousAGI: Double): InsurancePrem
}

enum class RelationToInsured {
    SELF, SPOUSE, DEPENDANT
}

enum class MPMedalType {
    BRONZE, SILVER, GOLD, PLATINUM
}

enum class MPPlanType {
    HMO, EPO, PPO
}

