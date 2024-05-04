package config

import YearMonth
import asset.AssetProgression
import cashflow.CashFlowEventConfig
import departed.DepartureConfig
import departed.NeverDepartConfig
import expense.ExpenseProgression
import income.IncomeProgression
import medical.MedInsuranceProgression
import socsec.SSBenefitProgression
import socsec.SecondarySSBenefitProgression
import util.currentDate

interface PersonConfigBuilder {
    fun departureConfig(person: Person): DepartureConfig = NeverDepartConfig()
    fun incomes(person: Person): List<IncomeProgression> = ArrayList()
    fun expenses(person: Person): List<ExpenseProgression> = ArrayList()
    fun assets(person: Person): List<AssetProgression> = ArrayList()
    fun benefits(person: Person): List<SSBenefitProgression> = ArrayList()
    fun secondaryBenefits(person: Person): List<SecondarySSBenefitProgression> = ArrayList()
    fun medInsurance(person: Person): List<MedInsuranceProgression> = ArrayList()
    fun cashFlowEvents(person: Person): List<CashFlowEventConfig> = ArrayList()
    fun targetSSDraw(): YearMonth = YearMonth(currentDate.year)
    fun targetRetirement(): YearMonth = YearMonth(currentDate.year)

    fun buildConfig(person: Person) = PersonConfig(
        person = person,
        departureConfig = departureConfig(person),
        incomes = incomes(person),
        expenses = expenses(person),
        assets = assets(person),
        benefits = benefits(person),
        secondaryBenefits = secondaryBenefits(person),
        medInsurance = medInsurance(person),
        cashFlowEvents = cashFlowEvents(person),
        targetSSDraw = targetSSDraw(),
        targetRetirement = targetRetirement()
    )
}

interface HouseholdConfigBuilder {
    fun buildConfig(householdMembers: List<PersonConfig>) = HouseholdConfig(
        members = householdMembers,
        expenses = expenses(),
        jointAssets = assets()
    )

    fun expenses(): List<ExpenseProgression> = ArrayList()
    fun assets(): List<AssetProgression> = ArrayList()
}