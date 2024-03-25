package config

import asset.AssetConfigProgression
import expense.ExpenseConfigProgression
import income.IncomeConfigProgression
import medical.MedInsuranceProgression
import socsec.SSBenefitConfigProgression

interface ConfigBuilder {
    fun buildConfig(): SimConfig
}

interface PersonConfigBuilder {
    fun incomes(person: Person): List<IncomeConfigProgression> = ArrayList()
    fun expenses(person: Person): List<ExpenseConfigProgression> = ArrayList()
    fun assets(person: Person): List<AssetConfigProgression> = ArrayList()
    fun benefits(person: Person): List<SSBenefitConfigProgression> = ArrayList()
    fun medInsurance(person: Person): List<MedInsuranceProgression> = ArrayList()
}

interface ParentConfigBuilder : PersonConfigBuilder {
    fun buildConfig(person: Person) = ParentConfig(
        person = person,
        incomes = incomes(person),
        expenses = expenses(person),
        assets = assets(person),
        benefits = benefits(person),
        medInsurance = medInsurance(person)
    )
}

interface DependentConfigBuilder : PersonConfigBuilder {
    fun buildConfig(person: Person) = DependantConfig(
        person = person,
        incomes = incomes(person),
        expenses = expenses(person),
        assets = assets(person),
        benefits = benefits(person),
        medInsurance = medInsurance(person)
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