package config

import Name
import YearMonth
import expense.ExpenseConfigProgression
import income.IncomeConfigProgression

interface Person {
    fun name(): Name
    fun birthYM(): YearMonth
    fun incomes(): ArrayList<IncomeConfigProgression>
    fun expenses(): ArrayList<ExpenseConfigProgression>
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
//    val employment: ArrayList<EmploymentConfig>,
    val otherIncomes: ArrayList<IncomeConfigProgression> = ArrayList(),
    val expenses: ArrayList<ExpenseConfigProgression> = ArrayList(),
//    val drawSSYM: YearMonth,
//    val medicareYM: YearMonth,
//    val socSec: SocSecConfig,
//    val assets: ArrayList<AssetConfig>,
) : Person {
    override fun name(): Name = name
    override fun birthYM(): YearMonth = birthYM
    override fun incomes(): ArrayList<IncomeConfigProgression> = otherIncomes
//        ArrayList(employment.map {it.incomeConfig} + otherIncomes)

    override fun expenses(): ArrayList<ExpenseConfigProgression> = expenses
}

data class Dependant(
    val name: Name,
    val birthYM: YearMonth,
    val contribIncomes: ArrayList<IncomeConfigProgression> = ArrayList(),
    val contribExpenses: ArrayList<ExpenseConfigProgression> = ArrayList(),
) : Person {
    override fun name(): Name = name
    override fun birthYM(): YearMonth = birthYM
    override fun incomes(): ArrayList<IncomeConfigProgression> = contribIncomes
    override fun expenses(): ArrayList<ExpenseConfigProgression> = contribExpenses
}

