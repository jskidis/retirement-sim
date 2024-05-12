import asset.AssetChange
import asset.AssetRec
import departed.DepartedRec
import expense.ExpenseRec
import income.IncomeRec
import inflation.InflationRAC
import inflation.InflationRec
import socsec.SSBenefitRec
import tax.FilingStatus
import tax.TaxableAmounts
import tax.TaxesRec
import transfers.TransferRec

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
    val cashFlowEvents: List<AssetChange> = ArrayList(),
    val taxes: TaxesRec = TaxesRec(),
    val finalPassTaxes: TaxesRec = TaxesRec(),
    val netSpend: Amount = 0.0,
    val transfers: List<TransferRec> = ArrayList(),
    val departed: List<DepartedRec> = ArrayList(),
    val randomValues: Map<String, Double> = mapOf(),
    val compoundROR: Double = 1.0,
    val filingStatus: FilingStatus = FilingStatus.JOINTLY,
) {
    fun totalIncome() = incomes.sumOf { it.amount() }
    fun totalExpense() = expenses.sumOf { it.amount() }
    fun totalAssetValues() = assets.sumOf { it.finalBalance() }
    fun totalAssetValues(excludedAssets: List<RecIdentifier>) =
        assets.filterNot { excludedAssets.contains(it.ident) }.sumOf { it.finalBalance() }

    fun totalBenefits() = benefits.sumOf { it.amount() }
    fun totalAssetCashflow() = cashFlowEvents.sumOf{ it.cashflow }
    fun averageRor() = assets.sumOf { it.gains.amount } / assets.sumOf { it.startBal }
    fun netSpend() = netSpend

    fun netDistributions() = assets.sumOf { asset->
        asset.tributions.sumOf { it.amount }
    }
    override fun toString(): String = toJsonStr()
}

data class YearlySummary(
    val year: Year,
    val inflation: InflationRAC,
    val assetValue: Amount,
    val avgROR: Rate,
    val income: Amount,
    val benefits: Amount,
    val expenses: Amount,
    val cashflowEvents: Amount,
    val netSpend: Amount,
    val agi: Amount,
    val taxes: Amount,
    val payrollTaxes: Amount,
    val carryOverTaxes: Amount,
    val departed: List<DepartedRec>,
    val compoundROR: Double,
    ) {
    fun inflAdjAssets(): Amount = (assetValue - carryOverTaxes) / inflation.cmpdEnd
    companion object {
        fun fromDetail(detail: YearlyDetail): YearlySummary =
            YearlySummary(
                year = detail.year,
                inflation = detail.inflation.std,
                assetValue = detail.totalAssetValues(),
                avgROR = detail.averageRor(),
                income = detail.totalIncome(),
                benefits = detail.totalBenefits(),
                expenses = detail.totalExpense(),
                cashflowEvents = detail.totalAssetCashflow(),
                netSpend = detail.netSpend,
                agi = detail.finalPassTaxes.agi,
                taxes = detail.finalPassTaxes.fed + detail.finalPassTaxes.state,
                payrollTaxes = detail.finalPassTaxes.socSec + detail.finalPassTaxes.medicare,
                carryOverTaxes = detail.finalPassTaxes.total() - detail.taxes.total(),
                departed = detail.departed,
                compoundROR = detail.compoundROR
            )
    }
}

data class SimResult (
    val summaries: List<YearlySummary>
) {
    fun lastYear(): YearlySummary = summaries.last()
}




data class RecIdentifier(
    val name: Name,
    val person: Name
) {
    override fun toString(): String = toJsonStr()
}

interface AmountRec {
    val year: Year
    val ident: RecIdentifier
    fun amount(): Amount
    fun taxable(): TaxableAmounts
    fun retainRec(): Boolean = amount() != 0.0
}

data class YearMonth (
    val year: Year,
    val month: Int = 0,
) : Comparable<YearMonth> {
    fun toDec(): Double = year + monthFraction()
    fun monthFraction(): Double = month / 12.0

    override fun compareTo(other: YearMonth): Int =
        ((toDec() - other.toDec()) * 100).toInt()

    override fun toString() = toJsonStr()

    fun plusMonths(months: Int): YearMonth =
        YearMonth(
            year = year + (month + months) / 12,
            month = (month + months) % 12
        )
}

