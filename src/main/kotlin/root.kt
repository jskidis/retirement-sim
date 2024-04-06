import asset.AssetRec
import asset.RequiredDistHandler
import expense.ExpenseRec
import income.IncomeRec
import inflation.InflationRec
import socsec.SSBenefitRec
import tax.FilingStatus
import tax.TaxableAmounts
import tax.TaxesRec

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
    val finalPassTaxes: TaxesRec = TaxesRec(),
    val netSpend: Amount = 0.0,
    val randomValues: Map<String, Double> = mapOf(),
    val filingStatus: FilingStatus = FilingStatus.JOINTLY,
) {
    fun totalIncome() = incomes.sumOf { it.amount() }
    fun totalExpense() = expenses.sumOf { it.amount() }
    fun totalAssetValues() = assets.sumOf { it.finalBalance() }
    fun totalBenefits() = benefits.sumOf { it.amount() }
    fun netSpend() = netSpend

    fun reqDistributions() = incomes.filter {
        RequiredDistHandler.CHANGE_NAME.equals(it.ident.name)
    }.sumOf { it.amount() }

    fun netDistributions() = assets.sumOf { asset->
        asset.tributions.sumOf { it.amount }
    }
    override fun toString(): String = toJsonStr()
}

data class RecIdentifier(
    val name: Name,
    val person: Name
) {
    override fun toString(): String = toJsonStr()
}

interface AmountRec {
    fun year(): Year
    fun ident(): RecIdentifier
    fun amount(): Amount
    fun taxable(): TaxableAmounts
    fun retainRec(): Boolean
}

data class YearMonth (
    val year: Year,
    val month: Int = 0,
) {
    fun toDec(): Double = year + monthFraction()
    fun monthFraction(): Double = month / 12.0
}

