import asset.AssetRec
import config.AmountConfig
import expense.ExpenseRec
import income.IncomeRec
import inflation.InflationRec
import socsec.SSBenefitRec
import tax.FilingStatus
import tax.TaxableAmounts
import tax.TaxesRec
import util.PortionOfYearPast

typealias Year = Int
typealias Amount = Double
typealias Name = String
typealias Rate = Double

data class YearlyDetail(
    val year: Year,
    val inflation: InflationRec,
    val incomes: List<IncomeRec> = ArrayList(),
    val expenses: List<ExpenseRec> = ArrayList(),
    val assets: List<AssetRec> = ArrayList(),
    val benefits: List<SSBenefitRec> = ArrayList(),
    val taxes: List<TaxesRec> = ArrayList(),
    val rorRndGaussian: Double = 0.0,
    val filingStatus: FilingStatus = FilingStatus.JOINTLY,
) {
    fun totalIncome() = incomes.sumOf { it.amount } + benefits.sumOf { it.amount }
    fun totalExpense() = expenses.sumOf { it.amount }
    fun totalTaxes() = taxes.sumOf { it.total() }
    fun totalAssetValues() = assets.sumOf { it.finalBalance() }
    fun netSpend() = (1- PortionOfYearPast.calc(year)) *
        (totalIncome() - totalExpense() - totalTaxes())
}

interface AmountRec {
    fun year(): Year
    fun config(): AmountConfig
    fun taxable(): TaxableAmounts
    fun retainRec(): Boolean
}

data class YearMonth (
    val year: Year,
    val month: Int,
) {
    fun toDec(): Double = year + monthFraction()
    fun monthFraction(): Double = month / 12.0
}

