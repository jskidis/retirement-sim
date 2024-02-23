package config

import Name
import YearMonth
import asset.AssetConfigProgression
import expense.ExpenseConfigProgression
import income.IncomeConfigProgression

data class Person (
    val name: Name,
    val birthYM: YearMonth,
    val actuarialGender: ActuarialGender
)

enum class ActuarialGender { MALE, FEMALE }

open class PersonConfig(
    val person: Person,
    val incomes: List<IncomeConfigProgression>,
    val expenses: List<ExpenseConfigProgression>,
    val assets: List<AssetConfigProgression>
) {
    fun name(): Name = person.name
    fun birthYM(): YearMonth = person.birthYM
    fun actuarialGender(): ActuarialGender = person.actuarialGender
    fun incomes(): List<IncomeConfigProgression> = incomes
    fun expenses(): List<ExpenseConfigProgression> = expenses
    fun assets(): List<AssetConfigProgression> = assets
}

open class ParentConfig(
    person: Person,
    incomes: List<IncomeConfigProgression> = ArrayList(),
    expenses: List<ExpenseConfigProgression> = ArrayList(),
    assets: List<AssetConfigProgression> = ArrayList(),
) : PersonConfig(person, incomes, expenses, assets)

open class DependantConfig(
    person: Person,
    incomes: List<IncomeConfigProgression> = ArrayList(),
    expenses: List<ExpenseConfigProgression> = ArrayList(),
    assets: List<AssetConfigProgression> = ArrayList(),
) : PersonConfig(person, incomes, expenses, assets)

