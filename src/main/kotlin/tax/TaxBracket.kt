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

