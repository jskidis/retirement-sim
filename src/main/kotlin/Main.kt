import config.ConfigBuilder
import departed.DepartedRec
import util.*
import java.io.BufferedWriter
import java.io.File
import java.util.stream.Collectors
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    val configBuilder: ConfigBuilder =
        SimulationRun.javaClass.classLoader.loadClass(args[0])
            .getDeclaredConstructor().newInstance() as ConfigBuilder

    val numSims = if (args.size > 1) args[1].toInt() else 1
    val outputFilename = if (args.size > 2) args[2] else null

    if (numSims == 1) runSingle(configBuilder)
    else {
        val timeInMillis = measureTimeMillis {
            runMultiple(numSims, configBuilder, outputFilename)
        }
        println("Time Elapsed: ${twoDecimalFormat.format(timeInMillis / 1000.0)} seconds")
    }
}

private fun runSingle(configBuilder: ConfigBuilder) {
    RandomizerFactory.setSuppressRandom(true)
    val result = SimulationRun.runSim(configBuilder, true)
    println("")
    println("Result: ${moneyFormat.format(result.lastYear().inflAdjAssets())}")
}

private fun singleRunFromMultiple(num: Int, configBuilder: ConfigBuilder, writer: BufferedWriter?)
    : SimResult {
    val result = SimulationRun.runSim(configBuilder, false)
    if (writer != null) {
        result.summaries.forEach {
            writer.write(it.toCSV(num))
            writer.newLine()
        }
    }
    return result
}

private fun runMultiple(
    numSims: Int,
    configBuilder: ConfigBuilder,
    outputFilename: String?,
) {
    val writer: BufferedWriter? =
        if (outputFilename == null) null
        else File(outputFilename).bufferedWriter()

    if (writer != null) {
        writer.write(yearlySummaryHeaders())
        writer.newLine()
    }

    RandomizerFactory.setSuppressRandom(true)
    val config = configBuilder.buildConfig()
    val baseline = SimulationRun.runSim(configBuilder, false)
        .lastYear().inflAdjAssets()

    RandomizerFactory.setSuppressRandom(false)

    val runs = (1..numSims).toList().parallelStream().map { simNum ->
        singleRunFromMultiple(simNum, configBuilder, writer).lastYear()
    }.collect(Collectors.toList())

    writer?.close()

    val sorted = ((runs.map { it.inflAdjAssets() }).sorted())
    val maxYear = runs.maxOf { it.year }
    val median = sorted[runs.size / 2]
    val average = sorted.sumOf { it } / numSims
    val successPct = 100.0 * runs.filter { config.simSuccess.wasSuccessRun(it) }.size / numSims
    val brokePct = 100.0 * runs.filter {
        it.expenses - it.benefits > it.assetValue
    }.size / numSims

    val avgInflation = runs.sumOf {
        Math.pow(it.inflation.cmpdEnd, 1.0 / (it.year - currentDate.year)) - 1
    } / numSims
    val avgROR = runs.sumOf {
        Math.pow(it.compoundROR, 1.0 / (it.year - currentDate.year)) - 1
    } / numSims

    val departedRecs: List<DepartedRec> = runs.flatMap { it.departed }

    println("")
    println("Simulations: ${commaFormat.format(numSims)}")
    println("Success  %: ${twoDecimalFormat.format(successPct)}%")
    println("Go Broke %: ${twoDecimalFormat.format(brokePct)}%")
    println("Max Year  : $maxYear")
    println("====== People ======")
    config.household.members.filter { it.isPrimary() }.forEach { member ->
        val departed = departedRecs.filter { departed -> member.name() == departed.person }
        val avgYear = Math.round(1.0 * departed.sumOf { it.year } / departed.size).toInt()
        println("${member.name()} Avg YOD: $avgYear")
        println("${member.name()} Retirement: ${member.targetRetirement()}")
        println("${member.name()} SS Draw Dt: ${member.targetSSDraw()}")
    }
    println("====== Legacy ======")
    println("Target  : ${moneyFormat.format(1000000.0)}")
    println("Median  : ${moneyFormat.format(median)}")
    println("Average : ${moneyFormat.format(average)}")
    println("Baseline: ${moneyFormat.format(baseline)}")
    println("====== Stats ======")
    println("Avg Infl: ${twoDecimalFormat.format(avgInflation * 100)}%")
    println("Avg ROR : ${twoDecimalFormat.format(avgROR * 100)}%")
    println("")
}
