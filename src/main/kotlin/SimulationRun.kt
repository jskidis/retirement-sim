import asset.AssetProcessor
import asset.NetSpendAllocation
import config.ConfigBuilder
import config.SimConfig
import expense.ExpenseProcessor
import income.IncomeProcessor
import inflation.InflationProcessor
import socsec.SSBenefitsProcessor
import tax.TaxesProcessor
import util.moneyFormat
import util.yearFromPrevYearDetail

object SimulationRun {
    fun runSim(configBuilder: ConfigBuilder, outputYearDetails: Boolean = true): Boolean {
        val years = ArrayList<YearlyDetail>()
        var currYearDetail = generateYearlyDetail(configBuilder.buildConfig(), null)

        do  {
            years.add(currYearDetail)
            currYearDetail = generateYearlyDetail(configBuilder.buildConfig(), currYearDetail)
        } while (currYearDetail.year < 2063 && metCriteria(currYearDetail))

        if (outputYearDetails) {
            years.forEach {
                println(
                    "Year: ${it.year} " +
                        "Income=${moneyFormat.format(it.totalIncome())} " +
                        "Expense=${moneyFormat.format(it.totalExpense())} " +
                        "Assets=${moneyFormat.format(it.totalAssetValues())} " +
                        "Taxes=${moneyFormat.format((it.totalTaxes()))} " +
                        "Net Spend=${moneyFormat.format((it.netSpend()))} " +
                        "Incomes:${it.incomes} " +
                        "Benefits:${it.benefits} " +
                        "Expenses:${it.expenses} " +
                        "Assets:{${it.assets} " +
                        "Taxes:${it.taxes} "
                )
            }
        }
        return metCriteria(currYearDetail)
    }

    fun metCriteria(currYearDetail: YearlyDetail): Boolean {
        return currYearDetail.assets.sumOf {
            it.finalBalance()
        } > 1000.0
    }

    fun generateYearlyDetail(config: SimConfig, prevYear: YearlyDetail?): YearlyDetail {
        val year = yearFromPrevYearDetail(prevYear)
        val inflation = InflationProcessor.process(config, prevYear)
        val incomes = IncomeProcessor.process(config, prevYear)
        val expenses = ExpenseProcessor.process(config, prevYear)
        val benefits = SSBenefitsProcessor.process(config, prevYear)

        var currYear = YearlyDetail(year,
            inflation = inflation, incomes = incomes, expenses = expenses, benefits = benefits)

        val assets = AssetProcessor.process(config, prevYear)
        currYear = currYear.copy(assets = assets)

        val taxesRec = TaxesProcessor.processTaxes(currYear, config)
        currYear = currYear.copy(taxes = listOf(taxesRec))

        NetSpendAllocation.allocateNetSpend(currYear.netSpend(), currYear, config.assetOrdering)

        return currYear
    }
}