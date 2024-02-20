package asset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.maps.shouldBeEmpty

class LazyPortfolioLoaderTest : ShouldSpec ({
    should("loadPortfolios loads a (String, AssetROR) map from csv") {
        val result = LazyPortfolioLoader.loadPortfolios()
        result.size.shouldBeGreaterThan(10)
        result.filter {
            it.value.mean > 0.25 || it.value.stdDev > 0.25 ||
                it.value.mean < 0.0 || it.value.stdDev < 0.0
        }.shouldBeEmpty()
    }
})