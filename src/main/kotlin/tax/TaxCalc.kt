package tax

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
