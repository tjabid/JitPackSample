package tg.sdk.sca.presentation.utils

private const val DEFAULT_IBAN_SEPARATOR = " "
private const val DEFAULT_IBAN_PERIOD = 4

fun String?.formatIban(
    separator: String = DEFAULT_IBAN_SEPARATOR,
    period: Int = DEFAULT_IBAN_PERIOD
): String {

    if (this == null || (period <= 0 || period > this.length)) return ""

    val builder = StringBuilder()
    forEachIndexed { index, c ->
        if (index % period == 0 && index != 0) {
            builder.append(separator)
        }
        builder.append(c)
    }
    return builder.toString()
}

fun String?.toIbanMask(): String? {
    if (isNullOrEmpty()) return this
    return "****${this.replace(" ","").takeLast(4)}"
}

fun String?.toShortIbanMask(): String? {
    if (this == null) return this
    return "*${this.takeLast(4)}"
}