package asset

import org.apache.commons.csv.CSVRecord
import tax.CSVReader

object LazyPortfolioLoader {
    fun getReader(): CSVReader<Pair<String, RORProvider>> = CSVReader { it: CSVRecord ->
        it[0] to RORProvider(mean = it[1].toDouble(), stdDev = it[2].toDouble())
    }

    fun loadPortfolios(resourcePath: String = "tables/lazy-portfolios.csv"): Map<String, RORProvider> {
        return getReader().readCsvFromResource(resourcePath).toMap()
    }
}