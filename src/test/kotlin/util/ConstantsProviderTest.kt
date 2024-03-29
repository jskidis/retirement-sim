package util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan

class ConstantsProviderTest : FunSpec({

    test("getValues should return non-zero values for all enum values") {
        ConstantsProvider.KEYS.entries.forEach {
            ConstantsProvider.getValue(it).shouldBeGreaterThan(0.0)
        }
    }
})
