package tax

import Amount
import Rate

data class TaxBracket(
    val pct: Rate,
    val start: Amount = 0.0,
    val end: Amount = Double.MAX_VALUE) {
    fun size() = end - start
}

object CurrentFedTaxJointBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/current-fed-tax-joint.csv")
    }
}

object RollbackFedTaxJointBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/rollback-fed-tax-joint.csv")
    }
}

object CurrentFedTaxSingleBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/current-fed-tax-single.csv")
    }
}

object RollbackFedTaxSingleBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/rollback-fed-tax-joint.csv")
    }
}

object CurrentStateTaxJointBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/current-state-tax-joint.csv")
    }
}

object FutureStateTaxJointBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/future-state-tax-joint.csv")
    }
}

object CurrentStateTaxSingleBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/current-state-tax-single.csv")
    }
}

object FutureStateTaxSingleBrackets : BracketBasedTaxCalc {
    override val brackets: List<TaxBracket> by lazy {
        loadBrackets("tables/future-state-tax-single.csv")
    }
}




