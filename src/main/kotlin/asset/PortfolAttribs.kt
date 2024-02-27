package asset

import Name
import Rate
import util.fourDecimalFormat
import util.twoDecimalFormat

data class PortfolAttribs(
    val name: Name, val mean: Rate, val stdDev: Rate,
    val divid: Rate = .0000, val expRatio: Rate = .0000,
    val stockPct: Rate = 0.00, val bondPct: Rate = 0.00, val ulcerIndex: Rate = .0000
) {
    override fun toString(): String {
        return "${fourDecimalFormat.format(mean)}, ${fourDecimalFormat.format(stdDev)}, " +
            "${twoDecimalFormat.format(divid)}, ${twoDecimalFormat.format(expRatio)}, " +
            "${twoDecimalFormat.format(stockPct)}, ${twoDecimalFormat.format(bondPct)}, " +
            fourDecimalFormat.format(ulcerIndex)
    }
}
