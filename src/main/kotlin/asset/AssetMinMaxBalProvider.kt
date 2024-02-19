package asset

import Amount
import YearlyDetail

interface AssetMinMaxBalProvider {
    fun minBalance(currYear: YearlyDetail): Amount
    fun maxBalance(currYear: YearlyDetail): Amount
}

open class NoMinMaxBalProvider : AssetMinMaxBalProvider {
    override fun minBalance(currYear: YearlyDetail): Amount = 0.0
    override fun maxBalance(currYear: YearlyDetail): Amount = Amount.MAX_VALUE
}