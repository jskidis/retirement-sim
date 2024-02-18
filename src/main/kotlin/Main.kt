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
                "Income: ${moneyFormat.format(it.totalIncome())} " +
                "Expense: ${moneyFormat.format(it.totalExpense())} " +
                "Incomes: ${it.incomes} " +
                "Expenses: ${it.expenses} " +
                "Taxes: ${it.taxes} "
        )
    }

}

fun generateYearlyDetail(config: MainConfig, currYearDetail: YearlyDetail?): YearlyDetail {
    val year = if(currYearDetail == null) config.startYear else currYearDetail.year + 1
    val inflation = InflationProcessor.process(config, currYearDetail)
    val incomes = IncomeProcessor.process(config, currYearDetail)
    val expenses = ExpenseProcessor.process(config, currYearDetail)

    val currYear = YearlyDetail(year,
        inflation = inflation, incomes = incomes, expenses = expenses)

    val taxesRec = TaxesProcessor.processTaxes(currYear, config)
    currYear.taxes.add(taxesRec)

    return currYear
}


