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
    val taxes: TaxesRec = TaxesRec(),
    val carryOverTaxable: List<TaxableAmounts> = ArrayList(),
    val prevCOPenalty: Amount = 0.0,
    val carryOverPenalty: Amount = 0.0,
    val rorRndGaussian: Double = 0.0,
    val filingStatus: FilingStatus = FilingStatus.JOINTLY,
) {
    fun totalIncome() = incomes.sumOf { it.amount }
    fun totalExpense() = expenses.sumOf { it.amount }
    fun totalAssetValues() = assets.sumOf { it.finalBalance() }
    fun totalBenefits() = benefits.sumOf { it.amount }
    fun netSpend() = (1- PortionOfYearPast.calc(year)) *
        (totalIncome() + totalBenefits() - totalExpense() - taxes.total() - prevCOPenalty)
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

