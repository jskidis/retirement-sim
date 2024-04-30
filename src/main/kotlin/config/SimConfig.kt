package config

import Year
import YearlyDetail
import YearlySummary
import asset.AssetProgression
import cashflow.CashFlowEventConfig
import expense.ExpenseProgression
import income.IncomeProgression
import netspend.NetSpendAllocationConfig
import socsec.SSBenefitProgression
import socsec.SecondarySSBenefitProgression
import tax.TaxCalcConfig
import tax.TaxProcessorConfig
import tax.TaxesProcessor
import transfers.TransferGenerator

data class SimConfig(
    val startYear: Year,
    val household: HouseholdConfig,
    val inflationConfig: InflationConfig,
    val assetOrdering: NetSpendAllocationConfig,
    val taxCalcConfig: TaxCalcYearlyConfig,
    val taxesProcessor: TaxProcessorConfig = TaxesProcessor,
    val transferGenerators: List<TransferGenerator> = ArrayList(),
    val simSuccess: SimSuccess = BasicSimSuccess(),
) {
    fun incomeConfigs(prevYear: YearlyDetail?): List<IncomeProgression> =
        nonDepartedMembers(prevYear).flatMap { it.incomes() }

    fun expenseConfigs(prevYear: YearlyDetail?): List<ExpenseProgression> =
        household.expenses +
            nonDepartedMembers(prevYear).flatMap { it.expenses() }

    fun assetConfigs(prevYear: YearlyDetail?): List<AssetProgression> =
        household.jointAssets +
            nonDepartedMembers(prevYear).flatMap { it.assets() }

    fun primaryBenefitsConfigs(prevYear: YearlyDetail?): List<SSBenefitProgression> =
        nonDepartedMembers(prevYear).flatMap { it.benefits() }

    fun secondaryBenefitsConfigs(prevYear: YearlyDetail?): List<SecondarySSBenefitProgression> =
        nonDepartedMembers(prevYear).flatMap { it.secondaryBenefits() }

    fun cashFlowConfigs(prevYear: YearlyDetail?): List<CashFlowEventConfig> =
        nonDepartedMembers(prevYear).flatMap { it.cashFlowEvents() }

    fun currTaxConfig(currYear: YearlyDetail): TaxCalcConfig =
        taxCalcConfig.getConfigForYear(currYear.year)

    fun nonDepartedMembers(prevYear: YearlyDetail?) : List<PersonConfig> {
        val departedMembers = prevYear?.departed?.map { it.person } ?: listOf()
        return household.members.filter { !departedMembers.contains(it.name()) }
    }
}

fun interface SimSuccess {
    fun wasSuccessRun(yearlySummary: YearlySummary): Boolean
}

class BasicSimSuccess : SimSuccess {
    override fun wasSuccessRun(yearlySummary: YearlySummary): Boolean =
        yearlySummary.assetValue > 0.0
}

