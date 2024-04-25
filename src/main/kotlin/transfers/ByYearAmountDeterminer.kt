package transfers

import Amount
import Year
import YearlyDetail
import config.SimConfig

interface ByYearAmountDeterminer : TransferAmountDeterminer {
    fun yearToTransfer(): Year
    override fun determineTransferAmount(config: SimConfig, currYear: YearlyDetail): Amount =
        if (currYear.year != yearToTransfer()) 0.0
        else availableToTransfer(currYear)
}