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
        val config = configBuilder.buildConfig()
        var currYearDetail = generateYearlyDetail(config, null)

        do  {
            years.add(currYearDetail)
            currYearDetail = generateYearlyDetail(configBuilder.buildConfig(), currYearDetail)
        } while (currYearDetail.year <= 2060 && metCriteria(currYearDetail))

        if (outputYearDetails) {
            years.forEach {
                println(
                    "Year: ${it.year} " +
                        "Income=${moneyFormat.format(it.totalIncome())} " +
                        "Benefits=${moneyFormat.format(it.totalBenefits())} " +
                        "Expense=${moneyFormat.format(it.totalExpense())} " +
                        "Assets=${moneyFormat.format(it.totalAssetValues())} " +
                        "Inf Adj=${moneyFormat.format(it.totalAssetValues() / it.inflation.std.cmpdEnd)} " +
                        "Taxes=${it.taxes} " +
                        "Net Spend=${moneyFormat.format((it.netSpend()))} " +
                        "Incomes:${it.incomes} " +
                        "Benefits:${it.benefits} " +
                        "Expenses:${it.expenses} " +
                        "Assets:{${it.assets} " +
                        "Taxes:${it.taxes} " +
                        "Carryover:${it.carryOverTaxable} " +
                        "CO Penalty:${moneyFormat.format(it.carryOverPenalty)}"
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
        val prevCOPenalty = prevYear?.carryOverPenalty ?: 0.0

        var currYear = YearlyDetail(year,
            inflation = inflation, incomes = incomes, expenses = expenses,
            benefits = benefits, prevCOPenalty = prevCOPenalty)

        currYear = currYear.copy(assets = AssetProcessor.process(config, prevYear))

        val prevCarryOver = prevYear?.carryOverTaxable ?: ArrayList()
        val taxesRec = TaxesProcessor.processTaxes(currYear, prevCarryOver, config)
        currYear = currYear.copy(taxes = taxesRec)

        NetSpendAllocation.allocateNetSpend(currYear.netSpend(), currYear, config.assetOrdering)

        currYear = currYear.copy(carryOverTaxable = TaxesProcessor.carryOverTaxable(currYear))
        currYear = currYear.copy(carryOverPenalty = TaxesProcessor.carryOverPenalty(currYear, config))

        return currYear
    }
}