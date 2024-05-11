import asset.AssetProcessor
import cashflow.CashFlowEventProcessor
import config.ConfigBuilder
import config.SimConfig
import departed.DepartureProcessor
import expense.ExpenseProcessor
import income.IncomeProcessor
import inflation.InflationProcessor
import medical.MedInsuranceProcessor
import netspend.NetSpendAllocation
import socsec.SSBenefitsProcessor
import transfers.TransferProcessor
import util.RandomizerFactory
import util.yearFromPrevYearDetail

object SimulationRun {
    fun runSim(
        configBuilder: ConfigBuilder,
        outputYearDetails: Boolean = true,
    ): SimResult {
        val years = ArrayList<YearlyDetail>()
        val config = configBuilder.buildConfig()

        var prevYear: YearlyDetail? = null
        do {
            val currYear = generateYearlyDetail(config, prevYear)
            years.add(currYear)
            prevYear = currYear
        } while (currYear.year < 2100 &&
            config.nonDepartedMembers(currYear).filter { it.isPrimary() }.isNotEmpty() &&
            currYear.totalAssetValues() > (currYear.totalExpense() - currYear.totalBenefits())
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
        val filingStatus = config.taxesProcessor.determineFilingStatus(prevYear, config)

        var currYear = YearlyDetail(
            year,
            inflation = inflation, incomes = incomes, expenses = expenses, assets = assets,
            benefits = benefits, filingStatus = filingStatus, randomValues = randomValues)

        val previousAGI = prevYear?.finalPassTaxes?.agi ?: config.household.initialAGI
        val medInsurance = MedInsuranceProcessor.process(config, currYear, previousAGI)
        currYear = currYear.copy(expenses = currYear.expenses + medInsurance)

        val cashflowEvents = CashFlowEventProcessor.process(config, prevYear, currYear)
        currYear = currYear.copy(cashFlowEvents = cashflowEvents)

        val secondaryBenefits = SSBenefitsProcessor.processSecondary(config, prevYear, currYear)
        currYear = currYear.copy(benefits = currYear.benefits + secondaryBenefits)

        val taxesProcessor = config.taxesProcessor
        val taxesRec = taxesProcessor.processTaxes(currYear, config)
        currYear = currYear.copy(taxes = taxesRec)

        val netSpend = NetSpendAllocation.determineNetSpend(currYear, prevYear)
        NetSpendAllocation.allocateNetSpend(netSpend, currYear, config.assetOrdering)
        currYear = currYear.copy(netSpend = netSpend)

        val departed = DepartureProcessor.process(config, prevYear, currYear)
        currYear = currYear.copy(departed = departed)

        currYear = currYear.copy(finalPassTaxes = taxesProcessor.processTaxes(currYear, config))
        currYear = currYear.copy(
            transfers = TransferProcessor.process(config, currYear),
            finalPassTaxes = taxesProcessor.processTaxes(currYear, config),
            compoundROR = (prevYear?.compoundROR ?: 1.0) * (1+ currYear.averageRor())
        )
        return currYear

    }
}