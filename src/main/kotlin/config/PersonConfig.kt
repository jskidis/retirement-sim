package config

import Name
import YearMonth
import asset.AssetProgression
import cashflow.CashFlowEventConfig
import departed.DepartureConfig
import expense.ExpenseProgression
import income.IncomeProgression
import medical.MedInsuranceProgression
import socsec.SSBenefitProgression
import socsec.SecondarySSBenefitProgression

data class Person (
    val name: Name,
    val birthYM: YearMonth,
    val actuarialGender: ActuarialGender,
    val isPrimary: Boolean,
)

enum class ActuarialGender { MALE, FEMALE }

open class PersonConfig(
    private val person: Person,
    private val departureConfig: DepartureConfig,
    private val incomes: List<IncomeProgression>,
    private val expenses: List<ExpenseProgression>,
    private val assets: List<AssetProgression>,
    private val benefits: List<SSBenefitProgression>,
    private val secondaryBenefits: List<SecondarySSBenefitProgression>,
    private val medInsurance: List<MedInsuranceProgression>,
    private val cashFlowEvents: List<CashFlowEventConfig>
) {
    fun name(): Name = person.name
    fun birthYM(): YearMonth = person.birthYM
    fun actuarialGender(): ActuarialGender = person.actuarialGender
    fun isPrimary(): Boolean = person.isPrimary
    fun departureConfig(): DepartureConfig = departureConfig
    fun incomes(): List<IncomeProgression> = incomes
    fun expenses(): List<ExpenseProgression> = expenses
    fun assets(): List<AssetProgression> = assets
    fun benefits(): List<SSBenefitProgression> = benefits
    fun secondaryBenefits(): List<SecondarySSBenefitProgression> = secondaryBenefits
    fun medInsurance(): List<MedInsuranceProgression> = medInsurance
    fun cashFlowEvents(): List<CashFlowEventConfig> = cashFlowEvents
}

