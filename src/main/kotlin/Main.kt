import config.ConfigBuilder
import util.RandomizerFactory
import util.moneyFormat
import java.time.LocalDateTime
import java.time.ZoneOffset

fun main(args: Array<String>) {
    val configBuilder: ConfigBuilder =
        SimulationRun.javaClass.classLoader.loadClass(args[0])
            .getDeclaredConstructor().newInstance() as ConfigBuilder

    val numSims = if (args.size > 1) args[1].toInt() else 1
    val legacy = if(args.size > 2) args[2].toDouble() else 1000.0

    if (numSims == 1) runSingle(configBuilder)
    else runMultiple(numSims, legacy, configBuilder)
}

private fun runSingle(configBuilder: ConfigBuilder) {
    RandomizerFactory.setSuppressRandom(true)
    val result = SimulationRun.runSim(configBuilder, true)
    println("")
    println("Result: ${moneyFormat.format(result.first)}")
}

private fun runMultiple(numSims: Int, legacy: Double, configBuilder: ConfigBuilder) {
    RandomizerFactory.setSuppressRandom(true)
    val baselineResult = SimulationRun.runSim(configBuilder, false).first

    RandomizerFactory.setSuppressRandom(false)
    val start = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    val runs = (1..numSims).map  {
        SimulationRun.runSim(configBuilder, false)
    }

    val sorted = ((runs.map { it.first }).sorted())
    val median = sorted[runs.size/2]
    val average = runs.sumOf { it.first } / numSims
    val successPct = 100.0 * sorted.filter{ it >= legacy }.size / numSims

    println("")
    println("Success Pct: $successPct")
    println("Median: ${moneyFormat.format(median)}")
    println("Average: ${moneyFormat.format(average)}")
    println("Baseline: ${moneyFormat.format(baselineResult)}")

//    val avgRand = runs.sumOf { it.second } / runs.size
//    println("Avg Rnd: $avgRand")

    val elapsed = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - start
    println("Elapsed: ${elapsed}")
}



