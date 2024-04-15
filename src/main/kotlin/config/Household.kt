package config

import Amount
import asset.AssetProgression
import expense.ExpenseProgression

data class HouseholdConfig(
    val members: List<PersonConfig>,
    val expenses: List<ExpenseProgression> = ArrayList(),
    val jointAssets: List<AssetProgression> = ArrayList(),
    val initialAGI: Amount = 0.0
)
