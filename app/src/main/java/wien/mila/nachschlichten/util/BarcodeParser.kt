package wien.mila.nachschlichten.util

object BarcodeParser {

    fun isValidEan(barcode: String): Boolean {
        return isValidEan13(barcode) || isValidEan8(barcode)
    }

    fun isValidEan13(barcode: String): Boolean {
        if (barcode.length != 13 || !barcode.all { it.isDigit() }) return false
        return checkDigitValid(barcode)
    }

    fun isValidEan8(barcode: String): Boolean {
        if (barcode.length != 8 || !barcode.all { it.isDigit() }) return false
        return checkDigitValid(barcode)
    }

    private fun checkDigitValid(barcode: String): Boolean {
        val digits = barcode.map { it.digitToInt() }
        val checkDigit = digits.last()
        val sum = digits.dropLast(1).mapIndexed { index, digit ->
            if (index % 2 == 0) digit else digit * 3
        }.sum()
        val expected = (10 - (sum % 10)) % 10
        return checkDigit == expected
    }
}
