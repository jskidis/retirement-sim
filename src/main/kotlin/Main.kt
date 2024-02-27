import asset.AssetProcessor
import asset.NetSpendAllocation
import config.SimConfig
import config.sample.Smiths
import expense.ExpenseProcessor
import income.IncomeProcessor
import inflation.InflationProcessor
import tax.TaxesProcessor
import util.moneyFormat
import util.yearFromPrevYearDetail

fun main(args: Array<String>) {
    val years = ArrayList<YearlyDetail>()

    var currYearDetail = generateYearlyDetail(Smiths.buildConfig(), null)

    while (currYearDetail.year < 2063) {
        years.add(currYearDetail)
        currYearDetail = generateYearlyDetail(Smiths.buildConfig(), currYearDetail)
    }

    years.forEach {
        println(
            "Year: ${it.year} " +
                "Income=${moneyFormat.format(it.totalIncome())} " +
                "Expense=${moneyFormat.format(it.totalExpense())} " +
                "Assets=${moneyFormat.format(it.totalAssetValues())} " +
                "Taxes=${moneyFormat.format((it.totalTaxes()))} " +
                "Net Spend=${moneyFormat.format((it.netSpend()))} " +
                "Incomes:${it.incomes} " +
                "Expenses:${it.expenses} " +
                "Assets:{${it.assets} " +
                "Taxes:${it.taxes} "
        )
    }

}

fun generateYearlyDetail(config: SimConfig, prevYear: YearlyDetail?): YearlyDetail {
    val year = yearFromPrevYearDetail(prevYear)
    val inflation = InflationProcessor.process(config, prevYear)
    val incomes = IncomeProcessor.process(config, prevYear)
    val expenses = ExpenseProcessor.process(config, prevYear)

    var currYear = YearlyDetail(year,
        inflation = inflation, incomes = incomes, expenses = expenses)

    val assets = AssetProcessor.process(config, prevYear)
    currYear = currYear.copy(assets = assets)

    val taxesRec = TaxesProcessor.processTaxes(currYear, config)
    currYear = currYear.copy(taxes = listOf(taxesRec))

    NetSpendAllocation.allocatedNetSpend(currYear)

    return currYear
}


