import asset.AssetProcessor
import asset.NetSpendAllocation
import config.ConfigBuilder
import config.SimConfig
import expense.ExpenseProcessor
import income.IncomeProcessor
import inflation.InflationProcessor
import medical.MedInsuranceProcessor
import socsec.SSBenefitsProcessor
import tax.TaxesProcessor
import util.RandomizerFactory
import util.moneyFormat
import util.yearFromPrevYearDetail

object SimulationRun {
    fun runSim(configBuilder: ConfigBuilder, outputYearDetails: Boolean = true): Triple<Boolean, Amount, Double> {
        val years = ArrayList<YearlyDetail>()
        val config = configBuilder.buildConfig()
        config.household.members.parent1
        var currYearDetail = generateYearlyDetail(config, null)

        do  {
            years.add(currYearDetail)
            currYearDetail = generateYearlyDetail(configBuilder.buildConfig(), currYearDetail)
        } while (currYearDetail.year <= 2060 && currYearDetail.totalAssetValues() > 0.0)

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
                        "Assets:${it.assets} " +
                        "Carryover:${it.carryOverTaxable} " +
                        "CO Penalty:${moneyFormat.format(it.carryOverPenalty)}" +
                    ""
                )
            }
        }

        val infAdjAssets = currYearDetail.totalAssetValues() / currYearDetail.inflation.std.cmpdEnd
        val avgRandom = years.sumOf {
            (it.randomValues[RandomizerFactory.GaussKeys.ROI.toString()]?:0.0) +
                (it.randomValues[RandomizerFactory.GaussKeys.INFLATION.toString()]?:0.0)
        } / years.size / 2.0
        return Triple((infAdjAssets > 1000000.0), infAdjAssets, avgRandom)
    }


    fun generateYearlyDetail(config: SimConfig, prevYear: YearlyDetail?): YearlyDetail {
        val year = yearFromPrevYearDetail(prevYear)
        val inflation = InflationProcessor.process(config, prevYear)
        val incomes = IncomeProcessor.process(config, prevYear)
        val expenses = ExpenseProcessor.process(config, prevYear)
        val assets = AssetProcessor.process(config, prevYear)
        val assetIncomes = assets.flatMap { it.incomeRecs() }
        val benefits = SSBenefitsProcessor.process(config, prevYear)
        val randomValues = RandomizerFactory.createNewValues(config)
        val prevCOPenalty = prevYear?.carryOverPenalty ?: 0.0

        var currYear = YearlyDetail(year,
            inflation = inflation, incomes = incomes + assetIncomes, expenses = expenses,
            assets = assets, benefits = benefits, randomValues = randomValues,
            prevCOPenalty = prevCOPenalty)

        val medInsurance = MedInsuranceProcessor.process(config, currYear)
        currYear = currYear.copy(expenses = currYear.expenses + medInsurance)

        val prevCarryOver = prevYear?.carryOverTaxable ?: ArrayList()
        val taxesRec = TaxesProcessor.processTaxes(currYear, prevCarryOver, config)
        currYear = currYear.copy(taxes = taxesRec)

        NetSpendAllocation.allocateNetSpend(currYear.netSpend(), currYear, config.assetOrdering)

        currYear = currYear.copy(carryOverTaxable = TaxesProcessor.carryOverTaxable(currYear))
        currYear = currYear.copy(carryOverPenalty = TaxesProcessor.carryOverPenalty(currYear, config))

        return currYear
    }
}