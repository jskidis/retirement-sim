package socsec

import Amount
import Rate
import util.RandomizerFactory

fun interface PayoutAdjProvider {
    fun adjustPayout(stdPay: Amount): Amount
}

class StdPayoutAdjProvider : PayoutAdjProvider {
    override fun adjustPayout(stdPay: Amount): Amount = stdPay
}

class RandomizedSSPayout : PayoutAdjProvider {
    val pct by lazy { determineRndPayoutPct() }

    override fun adjustPayout(stdPay: Amount): Amount = stdPay * pct

    private fun determineRndPayoutPct(): Rate =
        if (RandomizerFactory.suppressRandom()) 1.00
        else Math.min(1.00, Math.max(0.75, 0.5 + Math.random()))
}