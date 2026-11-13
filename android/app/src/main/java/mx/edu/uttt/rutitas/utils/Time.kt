package mx.edu.uttt.rutitas.utils

import java.security.MessageDigest
import java.text.NumberFormat
import java.util.Locale

object TimeUtils {
    private val regex = Regex("""^\s*(1[0-2]|0?[1-9]):([0-5][0-9])\s*(AM|PM)\s*$""", RegexOption.IGNORE_CASE)

    fun toMinutes(t: String): Int? {
        val m = regex.matchEntire(t) ?: return null
        var h = m.groupValues[1].toInt() % 12
        if (m.groupValues[3].uppercase() == "PM") h += 12
        return h * 60 + m.groupValues[2].toInt()
    }

    fun format(minutes: Int): String {
        val m = ((minutes % 1440) + 1440) % 1440
        val h24 = m / 60
        val h12 = if (h24 % 12 == 0) 12 else h24 % 12
        val mm = (m % 60).toString().padStart(2, '0')
        val suffix = if (h24 < 12) "AM" else "PM"
        return "$h12:$mm $suffix"
    }

    fun isValid(t: String) = toMinutes(t) != null
}

fun seatsText(n: Int): String = if (n == 1) "1 lugar" else "$n lugares"

fun money(n: Int): String = "$" + NumberFormat.getIntegerInstance(Locale.US).format(n)

fun sha256(text: String): String =
    MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
