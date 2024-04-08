import asset.AssetProcessor
import asset.RothConversionProcessor
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
    // Checking something with commit settings
    fun runSim(
        configBuilder: ConfigBuilder,
        outputYearDetails: Boolean = true,
    ): Pair<Amount, Double> {
        val years = ArrayList<YearlyDetail>()
        val config = configBuilder.buildConfig()
        config.household.members.parent1
        var currYearDetail = generateYearlyDetail(config, null)

        do {
            years.add(currYearDetail)
            currYearDetail = generateYearlyDetail(configBuilder.buildConfig(), currYearDetail)
        } while (currYearDetail.year <= 2060 && currYearDetail.totalAssetValues() > 0.0)

        if (outputYearDetails) {
            println("[")
            years.forEach { println(it.toString() + ", ") }
            println("]")
        }

        val infAdjAssets = currYearDetail.totalAssetValues() / currYearDetail.inflation.std.cmpdEnd
        val avgRandom = years.sumOf {
            (it.randomValues[RandomizerFactory.GaussKeys.ROI.toString()] ?: 0.0) +
                (it.randomValues[RandomizerFactory.GaussKeys.INFLATION.toString()] ?: 0.0)
        } / years.size / 2.0
        return Pair(infAdjAssets, avgRandom)
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

        var currYear = YearlyDetail(
            year,
            inflation = inflation, incomes = incomes + assetIncomes, expenses = expenses,
            assets = assets, benefits = benefits, randomValues = randomValues)

        val previousAGI = prevYear?.finalPassTaxes?.agi ?: config.household.initialAGI
        val medInsurance = MedInsuranceProcessor.process(config, currYear, previousAGI)
        currYear = currYear.copy(expenses = currYear.expenses + medInsurance.filter{it.retainRec()})

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