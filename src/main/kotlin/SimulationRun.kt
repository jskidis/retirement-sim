import asset.AssetProcessor
import asset.RothConversionProcessor
import cashflow.CashFlowEventProcessor
import config.ConfigBuilder
import config.SimConfig
import expense.ExpenseProcessor
import income.IncomeProcessor
import inflation.InflationProcessor
import medical.MedInsuranceProcessor
import netspend.NetSpendAllocation
import socsec.SSBenefitsProcessor
import util.RandomizerFactory
import util.yearFromPrevYearDetail

object SimulationRun {
    fun runSim(
        configBuilder: ConfigBuilder,
        outputYearDetails: Boolean = true,
    ): SimResult {
        val years = ArrayList<YearlyDetail>()
        val config = configBuilder.buildConfig()
        config.household.members.parent1

        var prevYear: YearlyDetail? = null

        do {
            years.add(generateYearlyDetail(config, prevYear))
            prevYear = years.last()
        } while (years.last().year < 2060 &&
            years.last().totalAssetValues() > years.last().totalExpense()
        )

        if (outputYearDetails) {
            println("[")
            years.forEach { println(it.toString() + ", ") }
            println("]")
        }

        return SimResult(years.map { YearlySummary.fromDetail(it) })
    }


    fun generateYearlyDetail(config: SimConfig, prevYear: YearlyDetail?): YearlyDetail {
        val year = yearFromPrevYearDetail(prevYear)
        val inflation = InflationProcessor.process(config, prevYear)
        val incomes = IncomeProcessor.process(config, prevYear)
        val expenses = ExpenseProcessor.process(config, prevYear)
        val assets = AssetProcessor.process(config, prevYear)
        val benefits = SSBenefitsProcessor.process(config, prevYear)
        val randomValues = RandomizerFactory.createNewValues(config)

        var currYear = YearlyDetail(
            year,
            inflation = inflation, incomes = incomes, expenses = expenses,
            assets = assets, benefits = benefits, randomValues = randomValues)

        val previousAGI = prevYear?.finalPassTaxes?.agi ?: config.household.initialAGI
        val medInsurance = MedInsuranceProcessor.process(config, currYear, previousAGI)
        currYear = currYear.copy(expenses = currYear.expenses + medInsurance.filter{it.retainRec()})

        val cashflowEvents = CashFlowEventProcessor.process(config, currYear)
        currYear = currYear.copy(cashFlowEvents = cashflowEvents)

        val taxesProcessor = config.taxesProcessor
        val taxesRec = taxesProcessor.processTaxes(currYear, config)
        currYear = currYear.copy(taxes = taxesRec)

        val netSpend = NetSpendAllocation.determineNetSpend(currYear, prevYear)
        NetSpendAllocation.allocateNetSpend(netSpend, currYear, config.assetOrdering)
        currYear = currYear.copy(netSpend = netSpend)

        currYear = currYear.copy(finalPassTaxes = taxesProcessor.processTaxes(currYear, config))
        RothConversionProcessor.process(config, currYear)
        currYear = currYear.copy(finalPassTaxes = taxesProcessor.processTaxes(currYear, config))

        return currYear
    }
}