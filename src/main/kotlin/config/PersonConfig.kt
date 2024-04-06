package config

import Name
import YearMonth
import asset.AssetProgression
import expense.ExpenseRec
import income.IncomeRec
import medical.MedInsuranceProgression
import progression.Progression
import socsec.SSBenefitConfigProgression

data class Person (
    val name: Name,
    val birthYM: YearMonth,
    val actuarialGender: ActuarialGender
)

enum class ActuarialGender { MALE, FEMALE }

open class PersonConfig(
    val person: Person,
    val incomes: List<Progression<IncomeRec>>,
    val expenses: List<Progression<ExpenseRec>>,
    val assets: List<AssetProgression>,
    val benefits: List<SSBenefitConfigProgression>,
    val medInsurance: List<MedInsuranceProgression>
) {
    fun name(): Name = person.name
    fun birthYM(): YearMonth = person.birthYM
    fun actuarialGender(): ActuarialGender = person.actuarialGender
    fun incomes(): List<Progression<IncomeRec>> = incomes
    fun expenses(): List<Progression<ExpenseRec>> = expenses
    fun assets(): List<AssetProgression> = assets
    fun benefits(): List<SSBenefitConfigProgression> = benefits
    fun medInsurance(): List<MedInsuranceProgression> = medInsurance
}

open class ParentConfig(
    person: Person,
    incomes: List<Progression<IncomeRec>> = ArrayList(),
    expenses: List<Progression<ExpenseRec>> = ArrayList(),
    assets: List<AssetProgression> = ArrayList(),
    benefits: List<SSBenefitConfigProgression> = ArrayList(),
    medInsurance: List<MedInsuranceProgression>
) : PersonConfig(person, incomes, expenses, assets, benefits, medInsurance)

open class DependantConfig(
    person: Person,
    incomes: List<Progression<IncomeRec>> = ArrayList(),
    expenses: List<Progression<ExpenseRec>> = ArrayList(),
    assets: List<AssetProgression> = ArrayList(),
    benefits: List<SSBenefitConfigProgression> = ArrayList(),
    medInsurance: List<MedInsuranceProgression>,
) : PersonConfig(person, incomes, expenses, assets, benefits, medInsurance)

