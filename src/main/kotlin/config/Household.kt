package config

import Amount
import asset.AssetProgression
import expense.ExpenseProgression

data class HouseholdConfig(
    val members: HouseholdMembers,
    val expenses: List<ExpenseProgression> = ArrayList(),
    val jointAssets: List<AssetProgression> = ArrayList(),
    val initialAGI: Amount = 0.0
)

data class HouseholdMembers(
    val parent1: ParentConfig,
    val parent2: ParentConfig,
    val dependants: List<DependantConfig> = ArrayList()
) {
    fun people(): ArrayList<PersonConfig> =
        ArrayList(listOf(parent1, parent2) + dependants)
}