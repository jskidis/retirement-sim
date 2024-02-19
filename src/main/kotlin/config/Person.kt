package config

import Name
import YearMonth
import asset.AssetConfigProgression
import expense.ExpenseConfigProgression
import income.IncomeConfigProgression

interface Person {
    fun name(): Name
    fun birthYM(): YearMonth
    fun incomes(): List<IncomeConfigProgression>
    fun expenses(): List<ExpenseConfigProgression>
    fun assets(): List<AssetConfigProgression>
}

data class HouseholdMembers(
    val parent1: Parent,
    val parent2: Parent,
    val dependants: List<Dependant> = ArrayList()
) {
    fun people(): ArrayList<Person> =
        ArrayList(listOf(parent1, parent2) + dependants)
}

data class Parent(
    val name: Name,
    val birthYM: YearMonth,
    var otherIncomes: List<IncomeConfigProgression> = ArrayList(),
    var expenses: List<ExpenseConfigProgression> = ArrayList(),
    var assets: List<AssetConfigProgression> = ArrayList(),
//    val employment: ArrayList<EmploymentConfig>,
//    val drawSSYM: YearMonth,
//    val medicareYM: YearMonth,
//    val socSec: SocSecConfig,
) : Person {
    override fun name(): Name = name
    override fun birthYM(): YearMonth = birthYM
    override fun incomes(): List<IncomeConfigProgression> = otherIncomes
    override fun expenses(): List<ExpenseConfigProgression> = expenses
    override fun assets(): List<AssetConfigProgression> = assets
}

data class Dependant(
    val name: Name,
    val birthYM: YearMonth,
    var contribIncomes: List<IncomeConfigProgression> = ArrayList(),
    var contribExpenses: List<ExpenseConfigProgression> = ArrayList(),
) : Person {
    override fun name(): Name = name
    override fun birthYM(): YearMonth = birthYM
    override fun incomes(): List<IncomeConfigProgression> = contribIncomes
    override fun expenses(): List<ExpenseConfigProgression> = contribExpenses
    override fun assets(): List<AssetConfigProgression> = ArrayList()
}

