package tax

import Amount
import Rate

data class BracketCase(
    val pct: Rate = 0.0,
    val start: Amount = 0.0,
    val end: Amount= Amount.MAX_VALUE,
) {
    fun size() = end - start
}

data class TaxBracket(
    val pct: Rate,
    val jointly: BracketCase = BracketCase(),
    val household: BracketCase = BracketCase(),
    val single: BracketCase = BracketCase()
)

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
