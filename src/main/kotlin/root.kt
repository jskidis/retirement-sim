import asset.AssetRec
import config.AmountConfig
import expense.ExpenseRec
import income.IncomeRec
import inflation.InflationRec
import socsec.SSBenefitRec
import tax.FilingStatus
import tax.TaxableAmounts
import tax.TaxesRec
import util.moneyFormat

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
    val secondPassTaxes: TaxesRec = TaxesRec(),
    val netSpend: Amount = 0.0,
    val randomValues: Map<String, Double> = mapOf(),
    val filingStatus: FilingStatus = FilingStatus.JOINTLY,
) {
    fun totalIncome() = incomes.sumOf { it.amount() }
    fun totalExpense() = expenses.sumOf { it.amount() }
    fun totalAssetValues() = assets.sumOf { it.finalBalance() }
    fun totalBenefits() = benefits.sumOf { it.amount() }
    fun netSpend() = netSpend

    override fun toString(): String =
        "{" +
            "\"year\": ${year}" +
            ", \"income\":\"${moneyFormat.format(totalIncome())}\"" +
            ", \"benefit\":\"${moneyFormat.format(totalBenefits())}\"" +
            ", \"expense\":\"${moneyFormat.format(totalExpense())}\"" +
            ", \"assetValue\":\"${moneyFormat.format(totalAssetValues())}\"" +
            ", \"infAdj\":\"${moneyFormat.format(totalAssetValues() / inflation.std.cmpdEnd)}\"" +
            ", \"netSpend\":\"${moneyFormat.format((netSpend()))}\"" +
            ", \"taxes\":${taxes}" +
            ", \"secondPass\":${secondPassTaxes}" +
            ", \"incomes\":${incomes}" +
            ", \"benefits\":${benefits}" +
            ", \"expenses\":${expenses}" +
            ", \"assets\":${assets}" +
            "}"
}

interface AmountRec {
    fun year(): Year
    fun config(): AmountConfig
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

