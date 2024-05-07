package socsec

import Amount
import Rate
import Year
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.SSTRUSTFUND_DEPLETED_YEAR
import util.RandomizerFactory

fun interface PayoutAdjProvider {
    fun adjustPayout(stdPay: Amount, year: Year): Amount
}

class StdPayoutAdjProvider : PayoutAdjProvider {
    override fun adjustPayout(stdPay: Amount, year: Year): Amount = stdPay
}

class RandomizedSSPayout : PayoutAdjProvider {
    val pct by lazy { determineRndPayoutPct() }

    override fun adjustPayout(stdPay: Amount, year: Year): Amount =
        if (year < ConstantsProvider.getValue(SSTRUSTFUND_DEPLETED_YEAR)) stdPay
        else stdPay * pct

    private fun determineRndPayoutPct(): Rate =
        if (RandomizerFactory.suppressRandom()) 1.00
        else Math.min(1.00, Math.max(5.0 / 6.0, (1.0 + Math.random()) * 2.0 / 3.0))
}