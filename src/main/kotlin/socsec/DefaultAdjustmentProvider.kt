package socsec

import Rate

fun interface DefaultAdjustmentProvider {
    fun initialAdjustment(): Rate
}
