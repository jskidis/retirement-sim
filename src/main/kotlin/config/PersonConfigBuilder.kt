package config

import asset.AssetConfigProgression
import expense.ExpenseConfigProgression
import income.IncomeConfigProgression

interface ConfigBuilder {
    fun buildConfig(): SimConfig
}

interface PersonConfigBuilder {
    fun incomes(person: Person): List<IncomeConfigProgression> = ArrayList()
    fun expenses(person: Person): List<ExpenseConfigProgression> = ArrayList()
    fun assets(person: Person): List<AssetConfigProgression> = ArrayList()
}

interface ParentConfigBuilder : PersonConfigBuilder {
    fun buildConfig(person: Person) = ParentConfig(
        person = person,
        incomes = incomes(person),
        expenses = expenses(person),
        assets = assets(person)
    )
}

interface DependentConfigBuilder : PersonConfigBuilder {
    fun buildConfig(person: Person) = DependantConfig(
        person = person,
        incomes = incomes(person),
        expenses = expenses(person),
        assets = assets(person)
    )
}

interface HouseholdConfigBuilder {
    fun buildConfig(householdMembers: HouseholdMembers) = HouseholdConfig(
        members = householdMembers,
        expenses = expenses(),
        jointAssets = assets()
    )

    fun expenses(): List<ExpenseConfigProgression> = ArrayList()
    fun assets(): List<AssetConfigProgression> = ArrayList()
}