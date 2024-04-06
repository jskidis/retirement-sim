package config

import asset.PortfolioAttribLoader
import asset.PortfolioAttribs

object AssetAttributeMap {
    val portfolioMap by lazy { PortfolioAttribLoader.loadPortfolios() }
    fun assetComp(name: String): PortfolioAttribs = portfolioMap[name]!!
}
