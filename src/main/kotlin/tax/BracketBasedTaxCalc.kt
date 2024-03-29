package tax

import Amount
import Rate
import YearlyDetail
import org.apache.commons.csv.CSVRecord
import util.CSVReader

interface BracketBasedTaxCalc : TaxCalculator {
    val brackets: List<TaxBracket>

    fun getReader(): CSVReader<TaxBracket> = CSVReader { it: CSVRecord ->
        TaxBracket(
            pct = it[0].toDouble(),
            jointly = BracketCase(
                it[0].toDouble(), it[1].toDouble(),
                if (!it[2].isNullOrEmpty()) it[2].toDouble() else Double.MAX_VALUE),
            household = BracketCase(
                it[0].toDouble(), it[3].toDouble(),
                if (!it[4].isNullOrEmpty()) it[4].toDouble() else Double.MAX_VALUE),
            single = BracketCase(
                it[0].toDouble(), it[5].toDouble(),
                if (!it[6].isNullOrEmpty()) it[6].toDouble() else Double.MAX_VALUE),
        )
    }

    fun loadBrackets(resourcePath: String): List<TaxBracket> {
        return getReader().readCsvFromResource(resourcePath).sortedBy { it.pct }
    }

    fun getCmpdInflation(currYear: YearlyDetail): Rate = currYear.inflation.std.cmpdStart

    override fun determineTax(taxableAmount: Amount, currYear: YearlyDetail)
        : Amount {

        val bracketsBFS = bracketsByFilingStatus(currYear.filingStatus)
        val cmpdInflation = getCmpdInflation(currYear)

        val inflationAdjAmount = Math.max(taxableAmount, 0.0) / cmpdInflation
        return bracketsBFS.fold(initial = 0.0) { acc, bracket ->
            if (bracket.start > inflationAdjAmount) acc
            else acc + bracket.pct * (Math.min(bracket.end, inflationAdjAmount) - bracket.start)
        } * cmpdInflation
    }

    override fun marginalRate(taxableAmount: Amount, currYear: YearlyDetail)
        : Rate {

        val bracketsBFS = bracketsByFilingStatus(currYear.filingStatus)
        val cmpdInflation = getCmpdInflation(currYear)

        val inflationAdjAmount = taxableAmount / cmpdInflation
        return bracketsBFS.findLast {
            it.start < inflationAdjAmount && it.end > inflationAdjAmount
        }?.pct ?: 0.0
    }

    fun bracketsByFilingStatus(filingStatus: FilingStatus): List<BracketCase> =
        when (filingStatus) {
            FilingStatus.JOINTLY -> brackets.map { it.jointly }
            FilingStatus.HOUSEHOLD -> brackets.map { it.household }
            FilingStatus.SINGLE -> brackets.map { it.single }
        }
}

object CurrentFedTaxBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/current-fed-tax.csv")
    }
}

object RollbackFedTaxBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/rollback-fed-tax.csv")
    }
}

object CurrentStateTaxBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/current-state-tax.csv")
    }
}

object FutureStateTaxBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/future-state-tax.csv")
    }
}

object CurrentFedLTGCalc : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/current-capgains-tax.csv")
    }
}
