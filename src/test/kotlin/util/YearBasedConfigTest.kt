package util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class YearBasedConfigTest : ShouldSpec({

    should("getConfigForYear") {
        val config = YearBasedConfig(listOf(
            YearConfigPair(2024, 1),
            YearConfigPair(2026, 2),
            YearConfigPair(2028, 3),
        ))

        config.getConfigForYear(2024).shouldBe(1)
        config.getConfigForYear(2025).shouldBe(1)
        config.getConfigForYear(2026).shouldBe(2)
        config.getConfigForYear(2027).shouldBe(2)
        config.getConfigForYear(2028).shouldBe(3)
        config.getConfigForYear(2099).shouldBe(3)

        shouldThrow<RuntimeException>{ config.getConfigForYear(2020) }
    }
})
