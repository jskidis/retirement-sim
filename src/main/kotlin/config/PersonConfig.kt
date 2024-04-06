package config

import Name
import YearMonth
import asset.AssetProgression
import expense.ExpenseConfigProgression
import income.IncomeProgression
import medical.MedInsuranceProgression
import socsec.SSBenefitConfigProgression

data class Person (
    val name: Name,
    val birthYM: YearMonth,
    val actuarialGender: ActuarialGender
)

enum class ActuarialGender { MALE, FEMALE }

open class PersonConfig(
    val person: Person,
    val incomes: List<IncomeProgression>,
    val expenses: List<ExpenseConfigProgression>,
    val assets: List<AssetProgression>,
    val benefits: List<SSBenefitConfigProgression>,
    val medInsurance: List<MedInsuranceProgression>
) {
    fun name(): Name = person.name
    fun birthYM(): YearMonth = person.birthYM
    fun actuarialGender(): ActuarialGender = person.actuarialGender
    fun incomes(): List<IncomeProgression> = incomes
    fun expenses(): List<ExpenseConfigProgression> = expenses
    fun assets(): List<AssetProgression> = assets
    fun benefits(): List<SSBenefitConfigProgression> = benefits
    fun medInsurance(): List<MedInsuranceProgression> = medInsurance
}

open class ParentConfig(
    person: Person,
    incomes: List<IncomeProgression> = ArrayList(),
    expenses: List<ExpenseConfigProgression> = ArrayList(),
    assets: List<AssetProgression> = ArrayList(),
    benefits: List<SSBenefitConfigProgression> = ArrayList(),
    medInsurance: List<MedInsuranceProgression>
) : PersonConfig(person, incomes, expenses, assets, benefits, medInsurance)

open class DependantConfig(
    person: Person,
    incomes: List<IncomeProgression> = ArrayList(),
    expenses: List<ExpenseConfigProgression> = ArrayList(),
    assets: List<AssetProgression> = ArrayList(),
    benefits: List<SSBenefitConfigProgression> = ArrayList(),
    medInsurance: List<MedInsuranceProgression>,
) : PersonConfig(person, incomes, expenses, assets, benefits, medInsurance)

