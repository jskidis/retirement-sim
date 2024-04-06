package tax

import Amount
import Rate
import YearlyDetail
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import org.apache.commons.csv.CSVRecord
import util.CSVReader

interface BracketBasedTaxCalc : TaxCalculator {
    override fun determineTax(taxableAmount: Amount, currYear: YearlyDetail): Amount

    fun marginalRate(taxableAmount: Amount, currYear: YearlyDetail): Rate
    fun topOfCurrBracket(taxableAmount: Amount, currYear: YearlyDetail): Amount
    fun topAmountBelowPct(pct: Rate, currYear: YearlyDetail): Amount
}

interface StdBracketBasedTaxCalc : BracketBasedTaxCalc, TaxBracketProvider, CmpdInflationProvider {
    override fun determineTax(taxableAmount: Amount, currYear: YearlyDetail)
        : Amount {

        val bracketsBFS = bracketsByFilingStatus(currYear.filingStatus)
        val cmpdInflation = getCmpdInflationStart(currYear)

        val inflationAdjAmount = Math.max(taxableAmount, 0.0) / cmpdInflation
        return bracketsBFS.fold(initial = 0.0) { acc, bracket ->
            if (bracket.start > inflationAdjAmount) acc
            else acc + bracket.pct * (Math.min(bracket.end, inflationAdjAmount) - bracket.start)
        } * cmpdInflation
    }

    override fun marginalRate(taxableAmount: Amount, currYear: YearlyDetail): Rate =
        findCurrentBracket(taxableAmount, currYear)?.pct ?: 0.0

    override fun topOfCurrBracket(taxableAmount: Amount, currYear: YearlyDetail): Amount {
        val currBracket = findCurrentBracket(taxableAmount, currYear)
        return if (currBracket == null) 0.0
        else currBracket.end * getCmpdInflationStart(currYear)
    }

    override fun topAmountBelowPct(pct: Rate, currYear: YearlyDetail): Amount {
        val bracketsBFS = bracketsByFilingStatus(currYear.filingStatus)
        return bracketsBFS.findLast { it.pct <= pct }
            ?.let { it.end * getCmpdInflationStart(currYear) } ?: 0.0
    }

    private fun findCurrentBracket(taxableAmount: Amount, currYear: YearlyDetail): BracketCase? {
        val bracketsBFS = bracketsByFilingStatus(currYear.filingStatus)
        val cmpdInflation = getCmpdInflationStart(currYear)

        val inflationAdjAmount = taxableAmount / cmpdInflation
        return bracketsBFS.findLast {
            it.start < inflationAdjAmount && it.end > inflationAdjAmount
        }
    }

    private fun bracketsByFilingStatus(filingStatus: FilingStatus): List<BracketCase> =
        when (filingStatus) {
            FilingStatus.JOINTLY -> brackets.map { it.jointly }
            FilingStatus.HOUSEHOLD -> brackets.map { it.household }
            FilingStatus.SINGLE -> brackets.map { it.single }
        }
}

interface TaxBracketProvider {
    val brackets: List<TaxBracket>

    fun getReader(): CSVReader<TaxBracket> = CSVReader { it: CSVRecord ->
        TaxBracket(
            pct = it[0].toDouble(),
            single = BracketCase(
                it[0].toDouble(), it[1].toDouble(),
                if (!it[2].isNullOrEmpty()) it[2].toDouble() else Double.MAX_VALUE),
            jointly = BracketCase(
                it[0].toDouble(), it[3].toDouble(),
                if (!it[4].isNullOrEmpty()) it[4].toDouble() else Double.MAX_VALUE),
            household = BracketCase(
                it[0].toDouble(), it[5].toDouble(),
                if (!it[6].isNullOrEmpty()) it[6].toDouble() else Double.MAX_VALUE),
        )
    }

    fun loadBrackets(resourcePath: String): List<TaxBracket> {
        return getReader().readCsvFromResource(resourcePath).sortedBy { it.pct }
    }
}

object CurrentFedTaxBrackets : StdBracketBasedTaxCalc,
    CmpdInflationProvider by StdCmpdInflationProvider() {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/current-fed-tax.csv")
    }
}

object RollbackFedTaxBrackets : StdBracketBasedTaxCalc,
    CmpdInflationProvider by StdCmpdInflationProvider() {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/rollback-fed-tax.csv")
    }
}

object CurrentStateTaxBrackets : StdBracketBasedTaxCalc,
    CmpdInflationProvider by StdCmpdInflationProvider() {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/current-state-tax.csv")
    }
}

object FutureStateTaxBrackets : StdBracketBasedTaxCalc,
    CmpdInflationProvider by StdCmpdInflationProvider() {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/future-state-tax.csv")
    }
}

object CurrentFedLTGBrackets : StdBracketBasedTaxCalc,
    CmpdInflationProvider by StdCmpdInflationProvider() {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/current-capgains-tax.csv")
    }
}

object RollbackFedLTGBrackets : StdBracketBasedTaxCalc,
    CmpdInflationProvider by StdCmpdInflationProvider() {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/rollback-capgains-tax.csv")
    }
}
