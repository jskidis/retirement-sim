package util

import java.util.*

object ConstantsProvider {
    enum class KEYS {
        STD_DEDUCT_JOINTLY { override fun toString() = "stdDeductJointly" },
        STD_DEDUCT_SINGLE { override fun toString() = "stdDeductSingle" },
        STD_DEDUCT_HOUSEHOLD { override fun toString() = "stdDeductHousehold" },
        SS_INCOME_CAP { override fun toString() = "ssIncomeCap" },
        MARKETPLACE_BASE_PREM { override fun toString() = "marketplaceBasePrem" },
        DENTAL_BASE_PREM { override fun toString() = "dentalBasePrem" },
        CONTRIB_LIMIT_401K { override fun toString() = "contribLimit401k" },
        CATCHUP_LIMIT_401K { override fun toString() = "catchupLimit401k" },
        CONTRIB_LIMIT_IRA { override fun toString() = "contribLimitIRA" },
        CATCHUP_LIMIT_IRA { override fun toString() = "catchupLimitIRA" },
        ROTH_INCOME_LIMIT_SINGLE { override fun toString() = "rothIncomeLimitSingle" },
        ROTH_INCOME_LIMIT_JOINTLY { override fun toString() = "rothIncomeLimitJointly" },
        RETIREMENT_CATCHUP_AGE { override fun toString() = "retirementCatchupAge" },
        RETIREMENT_WITHDRAW_AGE { override fun toString() = "retirementWithdrawAge"},
        SSTRUSTFUND_DEPLETED_YEAR { override fun toString() = "ssTrustfundDepletedYear" }
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