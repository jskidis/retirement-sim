package asset

import org.apache.commons.csv.CSVRecord
import tax.CSVReader

object LazyPortfolioLoader {
    fun getReader(): CSVReader<Pair<String, LazyPortfolioRORProvider>> =
        CSVReader { it: CSVRecord ->
            it[0].trim() to LazyPortfolioRORProvider(
                mean = it[1].toDouble(),
                stdDev = it[2].toDouble(),
                divid = it[3].toDouble(),
                expRatio = it[4].toDouble(),
                stockPct = it[5].toDouble(),
                bondPct = it[6].toDouble(),
                ulcerIndex = it[7].toDouble())
        }

    fun loadPortfolios(resourcePath: String = "tables/lazy-portfolios.csv")
        : Map<String, LazyPortfolioRORProvider> {
        return getReader().readCsvFromResource(resourcePath).toMap()
    }
}