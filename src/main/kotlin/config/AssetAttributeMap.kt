package config

import asset.PortfolAttribLoader
import asset.PortfolAttribs

object AssetAttributeMap {
    val portfolioMap by lazy { PortfolAttribLoader.loadPortfolios() }
    fun assetComp(name: String): PortfolAttribs = portfolioMap[name]!!
}
