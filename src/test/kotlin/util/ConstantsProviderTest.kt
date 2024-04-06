package util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan

class ConstantsProviderTest : ShouldSpec({

    should("getValues should return non-zero values for all enum values") {
        ConstantsProvider.KEYS.entries.forEach {
            ConstantsProvider.getValue(it).shouldBeGreaterThan(0.0)
        }
    }
})
