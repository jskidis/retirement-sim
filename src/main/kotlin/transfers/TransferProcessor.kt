package transfers

import YearlyDetail
import config.SimConfig

object TransferProcessor {
    fun process(config: SimConfig, currYear: YearlyDetail): List<TransferRec> =
        config.transferGenerators.flatMap {
            val transferInfo = it.determineTransferInfo(config, currYear)
            if (transferInfo == null) listOf()
            else it.performTransfers(currYear, transferInfo)
        }
}