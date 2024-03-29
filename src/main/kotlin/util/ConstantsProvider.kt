package util

import java.util.*

object ConstantsProvider {
    enum class KEYS {
        STD_DEDUCT_JOINTLY { override fun toString() = "stdDeductJointly" },
        STD_DEDUCT_SINGLE { override fun toString() = "stdDeductSingle" },
        STD_DEDUCT_HOUSEHOLD { override fun toString() = "stdDeductHousehold" },
        SS_INCOME_CAP { override fun toString() = "ssIncomeCap" },
        MARKETPLACE_BASE_PREM { override fun toString() = "marketplaceBasePrem" },
        MEDICARE_BASE_PREM { override fun toString() = "medicareBasePrem" },
    }

    fun getValue(key: KEYS): Double =
        properties.getProperty(key.toString()).toDouble()

    private val properties: Properties by lazy { load() }

    private fun load(): Properties {
        val p = Properties()
        p.load(javaClass.classLoader.getResourceAsStream(
            "properties/constant-values.properties"))
        return p
    }
}