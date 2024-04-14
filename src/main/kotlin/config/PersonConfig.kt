package config

import Name
import YearMonth
import asset.AssetProgression
import expense.ExpenseProgression
import expense.ExpenseRec
import income.IncomeProgression
import medical.MedInsuranceProgression
import progression.Progression
import socsec.SSBenefitProgression
import socsec.SecondarySSBenefitProgression

data class Person (
    val name: Name,
    val birthYM: YearMonth,
    val actuarialGender: ActuarialGender
)

enum class ActuarialGender { MALE, FEMALE }

open class PersonConfig(
    val person: Person,
    val incomes: List<IncomeProgression>,
    val expenses: List<Progression<ExpenseRec>>,
    val assets: List<AssetProgression>,
    val benefits: List<SSBenefitProgression>,
    val secondaryBenefits: List<SecondarySSBenefitProgression>,
    val medInsurance: List<MedInsuranceProgression>
) {
    fun name(): Name = person.name
    fun birthYM(): YearMonth = person.birthYM
    fun actuarialGender(): ActuarialGender = person.actuarialGender
    fun incomes(): List<IncomeProgression> = incomes
    fun expenses(): List<ExpenseProgression> = expenses
    fun assets(): List<AssetProgression> = assets
    fun benefits(): List<SSBenefitProgression> = benefits
    fun secondaryBenefits(): List<SecondarySSBenefitProgression> = secondaryBenefits
    fun medInsurance(): List<MedInsuranceProgression> = medInsurance
}

open class ParentConfig(
    person: Person,
    incomes: List<IncomeProgression>,
    expenses: List<ExpenseProgression>,
    assets: List<AssetProgression>,
    benefits: List<SSBenefitProgression>,
    secondaryBenefits: List<SecondarySSBenefitProgression>,
    medInsurance: List<MedInsuranceProgression>,
) : PersonConfig(person, incomes, expenses, assets, benefits, secondaryBenefits, medInsurance)

open class DependantConfig(
    person: Person,
    incomes: List<IncomeProgression>,
    expenses: List<ExpenseProgression>,
    assets: List<AssetProgression>,
    benefits: List<SSBenefitProgression>,
    secondaryBenefits: List<SecondarySSBenefitProgression>,
    medInsurance: List<MedInsuranceProgression>,
) : PersonConfig(person, incomes, expenses, assets, benefits, secondaryBenefits, medInsurance)

