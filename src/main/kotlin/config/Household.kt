package config

import asset.AssetConfigProgression
import expense.ExpenseConfigProgression

data class HouseholdConfig(
    val members: HouseholdMembers,
    val expenses: List<ExpenseConfigProgression> = ArrayList(),
    val jointAssets: List<AssetConfigProgression> = ArrayList(),
)

data class HouseholdMembers(
    val parent1: ParentConfig,
    val parent2: ParentConfig,
    val dependants: List<DependantConfig> = ArrayList()
) {
    fun people(): ArrayList<PersonConfig> =
        ArrayList(listOf(parent1, parent2) + dependants)
}