package asset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.ints.shouldBeGreaterThan

class LazyPortfolioLoaderTest : ShouldSpec({
    should("loadPortfolios loads a (String, AssetROR) map from csv") {
        val result = PortfolAttribLoader.loadPortfolios()
        result.size.shouldBeGreaterThan(10)
        result.values.filter {
            it.mean > 0.25 || it.stdDev > 0.25 ||
                it.divid > 0.10 || it.expRatio > 0.01 || it.ulcerIndex > 0.50 ||
                it.mean < 0.0 || it.stdDev < 0.0 ||
                it.divid < 0.0 || it.expRatio < 0.0 || it.ulcerIndex < 0.0 ||
                it.stockPct + it.bondPct > 1.0
        }.shouldBeEmpty()

/*
        result.toList().forEach {
            println("${it.first}, ${it.second}")
        }
*/
    }
})