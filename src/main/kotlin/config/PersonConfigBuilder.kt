package config

import asset.AssetProgression
import expense.ExpenseProgression
import income.IncomeProgression
import medical.MedInsuranceProgression
import socsec.SSBenefitProgression
import socsec.SecondarySSBenefitProgression

interface ConfigBuilder {
    fun buildConfig(): SimConfig
}

interface PersonConfigBuilder {
    fun incomes(person: Person): List<IncomeProgression> = ArrayList()
    fun expenses(person: Person): List<ExpenseProgression> = ArrayList()
    fun assets(person: Person): List<AssetProgression> = ArrayList()
    fun benefits(person: Person): List<SSBenefitProgression> = ArrayList()
    fun secondaryBenefits(person: Person): List<SecondarySSBenefitProgression> = ArrayList()
    fun medInsurance(person: Person): List<MedInsuranceProgression> = ArrayList()
}

interface ParentConfigBuilder : PersonConfigBuilder {
    fun buildConfig(person: Person) = ParentConfig(
        person = person,
        incomes = incomes(person),
        expenses = expenses(person),
        assets = assets(person),
        benefits = benefits(person),
        secondaryBenefits = secondaryBenefits(person),
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
        secondaryBenefits = secondaryBenefits(person),
        medInsurance = medInsurance(person)
    )
}

interface HouseholdConfigBuilder {
    fun buildConfig(householdMembers: HouseholdMembers) = HouseholdConfig(
        members = householdMembers,
        expenses = expenses(),
        jointAssets = assets()
    )

    fun expenses(): List<ExpenseProgression> = ArrayList()
    fun assets(): List<AssetProgression> = ArrayList()
}