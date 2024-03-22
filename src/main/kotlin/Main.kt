import config.ConfigBuilder
import util.moneyFormat
import java.time.LocalDateTime
import java.time.ZoneOffset

fun main(args: Array<String>) {
    val configBuilder: ConfigBuilder =
        SimulationRun.javaClass.classLoader.loadClass(args[0])
            .getDeclaredConstructor().newInstance() as ConfigBuilder

    val numSims = if (args.size > 1) args[1].toInt() else 1
    val outputYearly = if (args.size > 2) args[2].toBoolean() else true

    val start = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    val runs = (1..numSims).map  {
        SimulationRun.runSim(configBuilder, outputYearly)
    }

    val numSuccessful = runs.filter { it.first }
    val median = ((runs.map { it.second }).sorted())[numSims / 2]

    println("")
    println("Success:${numSuccessful.count() * 100.0 / numSims}")
    println("Median: ${moneyFormat.format(median)}")

    val elapsed = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - start
    println("Elapsed: ${elapsed}")
}



