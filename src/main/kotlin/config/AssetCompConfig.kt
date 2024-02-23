package config

import Rate
import asset.AssetComposition
import asset.LazyPortfolioLoader

object AssetCompConfig {
    val portfolioMap = LazyPortfolioLoader.loadPortfolios()
    fun assetComp(name: String, pct: Rate = 1.0): AssetComposition =
        AssetComposition(name = name, pct = pct, rorProvider = portfolioMap[name]!!)
}