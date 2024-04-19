import asset.AssetProcessor
import cashflow.CashFlowEventProcessor
import config.ConfigBuilder
import config.SimConfig
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
        } while (currYear.year < 2050 &&
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

        var currYear = YearlyDetail(
            year,
            inflation = inflation, incomes = incomes, expenses = expenses,
            assets = assets, benefits = benefits, randomValues = randomValues)

        val previousAGI = prevYear?.finalPassTaxes?.agi ?: config.household.initialAGI
        val medInsurance = MedInsuranceProcessor.process(config, currYear, previousAGI)
        currYear = currYear.copy(expenses = currYear.expenses + medInsurance)

        val cashflowEvents = CashFlowEventProcessor.process(config, currYear)
        currYear = currYear.copy(cashFlowEvents = cashflowEvents)

        val secondaryBenefits = SSBenefitsProcessor.processSecondary(config, prevYear, currYear)
        currYear = currYear.copy(benefits = currYear.benefits + secondaryBenefits)

        val taxesProcessor = config.taxesProcessor
        val taxesRec = taxesProcessor.processTaxes(currYear, config)
        currYear = currYear.copy(taxes = taxesRec)

        val netSpend = NetSpendAllocation.determineNetSpend(currYear, prevYear)
        NetSpendAllocation.allocateNetSpend(netSpend, currYear, config.assetOrdering)
        currYear = currYear.copy(netSpend = netSpend)

        currYear = currYear.copy(finalPassTaxes = taxesProcessor.processTaxes(currYear, config))
        currYear = currYear.copy(
            transfers = TransferProcessor.process(config, currYear),
            finalPassTaxes = taxesProcessor.processTaxes(currYear, config)
        )
/*
        val rothBalances = assets.filter {
            it.assetType == AssetType.ROTH || it.assetType == AssetType.ROTH401K
        }.sumOf {it.finalBalance() }
        val iraBalances = assets.filter {
            it.assetType == AssetType.IRA || it.assetType == AssetType.STD401K
        }.sumOf {it.finalBalance() }
        val nraBalances = assets.filter {
            it.assetType == AssetType.NRA
        }.sumOf {it.finalBalance() }
        val cashBalances = assets.filter {
            it.assetType == AssetType.CASH
        }.sumOf {it.finalBalance() }

        println("Year: ${currYear.year}, " +
            "IRA: ${moneyFormat.format(iraBalances)}, " +
            "ROTH: ${moneyFormat.format(rothBalances)}, " +
            "NRA: ${moneyFormat.format(nraBalances)}, " +
            "CASH: ${moneyFormat.format(cashBalances)}, "
        )
 */
        return currYear

    }
}