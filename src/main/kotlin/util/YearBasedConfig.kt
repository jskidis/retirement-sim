package util

import Year

open class YearBasedConfig<T> (val list: List<YearConfigPair<T>>) {
    fun getConfigForYear(year: Year): T =
        list.findLast { year >= it.startYear }
            ?.config
            ?: throw RuntimeException("Error loading yearly config")
}

data class YearConfigPair<T> (
    val startYear: Year,
    val config: T
)

fun <T> SingleYearBasedConfig(config: T): YearBasedConfig<T> =
    YearBasedConfig(listOf(YearConfigPair(currentDate.year, config)))