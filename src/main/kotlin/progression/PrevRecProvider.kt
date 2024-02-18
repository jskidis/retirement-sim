package progression

import YearlyDetail

interface PrevRecProvider<T> {
    fun initialValue(): T
    fun previousValue(prevYear: YearlyDetail): T
}

interface PrevRecProviderProgression<T> :
    Progression<T>, PrevRecProvider<T> {

    fun next(prevVal: T): T
    override fun determineNext(prevYear: YearlyDetail?): T =
        if (prevYear == null) initialValue()
        else next(previousValue(prevYear))
}

