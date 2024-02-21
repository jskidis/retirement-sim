import asset.AssetProcessor
import config.MainConfig
import config.buildMyConfig
import expense.ExpenseProcessor
import income.IncomeProcessor
import inflation.InflationProcessor
import tax.TaxesProcessor

fun main(args: Array<String>) {
    val years = ArrayList<YearlyDetail>()

    var currYearDetail = generateYearlyDetail(buildMyConfig(), null)

    while (currYearDetail.year < 2063) {
        years.add(currYearDetail)
        currYearDetail = generateYearlyDetail(buildMyConfig(), currYearDetail)
    }

    years.forEach {
        println(
            "Year: ${it.year} " +
                "Income=${moneyFormat.format(it.totalIncome())} " +
                "Expense=${moneyFormat.format(it.totalExpense())} " +
                "Assets=${moneyFormat.format(it.totalAssetValues())} " +
                "Taxes=${moneyFormat.format((it.totalTaxes()))} " +
                "Incomes:${it.incomes} " +
                "Expenses:${it.expenses} " +
                "Assets:{${it.assets} " +
                "Taxes:${it.taxes} "
        )
    }

}

fun generateYearlyDetail(config: MainConfig, prevYear: YearlyDetail?): YearlyDetail {
    val year = if(prevYear == null) config.startYear else prevYear.year + 1
    val inflation = InflationProcessor.process(config, prevYear)
    val incomes = IncomeProcessor.process(config, prevYear)
    val expenses = ExpenseProcessor.process(config, prevYear)

    var currYear = YearlyDetail(year,
        inflation = inflation, incomes = incomes, expenses = expenses)

    val assets = AssetProcessor.process(config, prevYear, currYear)
    currYear = currYear.copy(assets = assets)

    val taxesRec = TaxesProcessor.processTaxes(currYear, config)
    currYear = currYear.copy(taxes = listOf(taxesRec))

    return currYear
}


