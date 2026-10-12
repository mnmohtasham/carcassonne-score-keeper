package com.mani.carcassonnescorekeeper.data

import java.util.Locale

/** Pawn colours as opaque ARGB values. Players may also pick any custom colour. */
object PawnPalette {
    val YELLOW = 0xFFF4C430.toInt()
    val GREEN = 0xFF2E8540.toInt()
    val BLUE = 0xFF1F5FBF.toInt()
    val BLACK = 0xFF222222.toInt()
    val RED = 0xFFD0342C.toInt()
    val PINK = 0xFFE0609E.toInt()

    /** The colours offered in the picker, in display order. */
    val presets = listOf(YELLOW, GREEN, BLUE, BLACK, RED, PINK)

    /** Assigned automatically once every preset is taken (7th and 8th player). */
    val extras = listOf(0xFF8A8A8A.toInt(), 0xFF7A4BB5.toInt())

    /** Save format v1 stored a palette index instead of a colour; this was that palette. */
    internal val legacyByIndex = listOf(RED, BLUE, GREEN, YELLOW, BLACK, PINK, 0xFF8A8A8A.toInt(), 0xFF7A4BB5.toInt())

    /** First preset (then extra) colour not in [used]; falls back to the first preset. */
    fun firstFree(used: Collection<Int>): Int = (presets + extras).firstOrNull { it !in used } ?: presets.first()

    fun toHex(argb: Int): String = String.format(Locale.ROOT, "#%06X", argb and 0xFFFFFF)

    /** Parses "#RRGGBB" or "RRGGBB" into an opaque colour, or null. */
    fun parseHex(text: String): Int? {
        val digits = text.trim().removePrefix("#")
        if (digits.length != 6) return null
        return digits.toIntOrNull(16)?.let { it or 0xFF000000.toInt() }
    }
}
