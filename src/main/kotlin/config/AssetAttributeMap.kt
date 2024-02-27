package config

import asset.PortfolAttribs
import asset.PortfolioAttribLoader

object AssetAttributeMap {
    val portfolioMap by lazy { PortfolioAttribLoader.loadPortfolios() }
    fun assetComp(name: String): PortfolAttribs = portfolioMap[name]!!
}
