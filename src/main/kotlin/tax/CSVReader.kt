package tax

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.InputStream

typealias csvRecToEntity<E> = (CSVRecord) -> E

class CSVReader<E>(val convertFunc: csvRecToEntity<E>) {
    fun readCsvFromResource(resourcePath: String): List<E> =
        readCsv(inputStream = javaClass.classLoader.getResourceAsStream(resourcePath)
                ?: throw RuntimeException("Unable to load resource file $resourcePath"),
        )

    private fun readCsv(inputStream: InputStream): List<E> =
        CSVFormat.Builder.create(CSVFormat.DEFAULT).apply {
            setIgnoreSurroundingSpaces(true)
            setIgnoreEmptyLines(true)
        }.build().parse(inputStream.reader()).map { convertFunc(it) }
}