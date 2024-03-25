package medical

import Amount
import progression.CYProgression

data class InsurancePrem(
    val name: String,
    val premium: Amount = 0.0,
    val monthsCovered: Int = 0,
    val fullyDeductAmount: Amount = 0.0
) {
//    fun hasCoverage(): Boolean = monthsCovered > 0
}

interface MedInsuranceProgression : CYProgression<InsurancePrem>

enum class RelationToInsured {
    SELF, SPOUSE, DEPENDANT
}

enum class MPMedalType {
    BRONZE, SILVER, GOLD, PLATINUM
}

enum class MPPlanType {
    HMO, EPO, PPO
}

