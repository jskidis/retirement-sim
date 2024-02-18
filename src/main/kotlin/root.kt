import expense.ExpenseRec
import income.IncomeRec
import inflation.InflationRec
import tax.TaxesRec
import java.text.DecimalFormat
import kotlin.random.Random
import kotlin.random.asJavaRandom

typealias Year = Int
typealias Amount = Double
typealias Name = String
typealias Rate = Double

data class YearlyDetail(
    val year: Year,
    val inflation: InflationRec,
    val incomes: ArrayList<IncomeRec> = ArrayList(),
    val expenses: ArrayList<ExpenseRec> = ArrayList(),
    val assets: ArrayList<AssetRec> = ArrayList(),
    val taxes: ArrayList<TaxesRec> = ArrayList(),
    var netSpend: Amount = 0.0
) {
    fun totalIncome() = incomes.sumOf { it.amount }
    fun totalExpense() = expenses.sumOf { it.amount }
}

data class YearMonth (
    val year: Year,
    val month: Int,
) {
    fun toDec(): Double = year + month / 12.0
}

data class DateRange(
    val start: YearMonth,
    val end: YearMonth
)

interface GaussianRndProvider {
    fun gaussianRndValue(): Double = Random.asJavaRandom().nextGaussian()
}

val moneyFormat = DecimalFormat("$##,###,###")

/*
data class RetireContribRec(
    val amount: Amount,
    val deductions: TaxableAmounts,
    val assetName: Name,
    val person: Name,
)
*/

