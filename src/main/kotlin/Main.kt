import config.ConfigBuilder
import java.time.LocalDateTime
import java.time.ZoneOffset

fun main(args: Array<String>) {
    val configBuilder: ConfigBuilder =
        SimulationRun.javaClass.classLoader.loadClass(args[0])
            .getDeclaredConstructor().newInstance() as ConfigBuilder

    val numSims = if (args.size > 1) args[1].toInt() else 1
    val outputYearly = if (args.size > 2) args[2].toBoolean() else true

    val start = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
    for (i in 1..numSims) {
        SimulationRun.runSim(configBuilder, outputYearly)
    }
    val elapsed = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - start
    println(elapsed)
}



