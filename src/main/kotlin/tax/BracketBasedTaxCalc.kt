package tax

import Amount
import Rate
import YearlyDetail
import config.MainConfig
import org.apache.commons.csv.CSVRecord

interface BracketBasedTaxCalc : TaxCalculator {
    val brackets: List<TaxBracket>

    fun getReader(): CSVReader<TaxBracket> = CSVReader { it: CSVRecord ->
        TaxBracket(
            pct = it[0].toDouble(),
            start = it[1].toDouble(),
            end = if (it.size() > 2 && !it[2].isNullOrEmpty()) it[2].toDouble() else Double.MAX_VALUE
        )
    }

    fun loadBrackets(resourcePath: String): List<TaxBracket> {
        return getReader().readCsvFromResource(resourcePath)
    }

    fun getCmpdInflation(currYear: YearlyDetail): Rate = currYear.inflation.chain.cmpdStart

    override fun determineTax(taxableAmount: Amount, config: MainConfig, currYear: YearlyDetail)
        : Amount {

        val cmpdInflation = getCmpdInflation(currYear)
        val inflationAdjAmount = taxableAmount / cmpdInflation
        return brackets.fold(initial = 0.0) { acc, bracket ->
            if (bracket.start > inflationAdjAmount) acc
            else acc + bracket.pct * (Math.min(bracket.end, inflationAdjAmount) - bracket.start)
        } * cmpdInflation
    }
}