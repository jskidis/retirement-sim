package socsec

import Rate

fun interface DefaultAdjustmentProvider {
    fun initialAdjustment(): Rate
}

class StdDefaultAdjustmentProvider: DefaultAdjustmentProvider {
    override fun initialAdjustment(): Rate = 0.0
}
