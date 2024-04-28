import config.ConfigBuilder
import util.RandomizerFactory
import util.moneyFormat
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
    else runMultiple(numSims, configBuilder, outputFilename)
}

private fun runSingle(configBuilder: ConfigBuilder) {
    RandomizerFactory.setSuppressRandom(true)
    val result = SimulationRun.runSim(configBuilder, true)
    println("")
    println("Result: ${moneyFormat.format(result.lastYear().inflAdjAssets())}")
}

private fun singleRunFromMultiple(num: Int, configBuilder: ConfigBuilder, writer: BufferedWriter?)
    : YearlySummary {
    val result = SimulationRun.runSim(configBuilder, false)
    if (writer != null) {
        result.summaries.forEach {
            writer.write(it.toCSV(num))
            writer.newLine()
        }
    }
    return result.lastYear()
}

private fun runMultiple(
    numSims: Int,
    configBuilder: ConfigBuilder,
    outputFilename: String?,
) {
    val timeInMillis = measureTimeMillis {
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
            singleRunFromMultiple(simNum, configBuilder, writer)
        }.collect(Collectors.toList())

        writer?.close()

        val sorted = ((runs.map { it.inflAdjAssets() }).sorted())
        val median = sorted[runs.size / 2]
        val average = sorted.sumOf { it } / numSims
        val successPct = 100.0 * runs.filter { config.simSuccess.wasSuccessRun(it) }.size / numSims
        val brokePct = 100.0 * runs.filter {
            it.expenses - it.benefits > it.assetValue
        }.size / numSims

        println("")
        println("Success Pct: $successPct")
        println("Broke Pct: $brokePct")
        println("Median: ${moneyFormat.format(median)}")
        println("Average: ${moneyFormat.format(average)}")
        println("Baseline: ${moneyFormat.format(baseline)}")
    }

    println("Elapsed: ${timeInMillis / 1000.0}")
}
