package config

import asset.AssetProgression
import expense.ExpenseRec
import income.IncomeRec
import medical.MedInsuranceProgression
import progression.Progression
import socsec.SSBenefitConfigProgression

interface ConfigBuilder {
    fun buildConfig(): SimConfig
}

interface PersonConfigBuilder {
    fun incomes(person: Person): List<Progression<IncomeRec>> = ArrayList()
    fun expenses(person: Person): List<Progression<ExpenseRec>> = ArrayList()
    fun assets(person: Person): List<AssetProgression> = ArrayList()
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

    fun employmentConfigs(person: Person): List<EmploymentConfig>
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

    fun expenses(): List<Progression<ExpenseRec>> = ArrayList()
    fun assets(): List<AssetProgression> = ArrayList()
}