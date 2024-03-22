package asset

import org.apache.commons.csv.CSVRecord
import util.CSVReader
import util.YearBasedConfig
import util.YearConfigPair

object RmdPct {
    val ageMap: YearBasedConfig<Double> by lazy {
        YearBasedConfig(loadMap())
    }

    fun loadMap(): List<YearConfigPair<Double>> =
        getReader().readCsvFromResource("tables/rmd.csv")


    fun getReader() = CSVReader { it: CSVRecord ->
        YearConfigPair( startYear = it[0].toInt(), config = it[1].toDouble() )
    }

    fun getRmdPct(age: Int): Double =
        ageMap.getConfigForYear(age)
}
