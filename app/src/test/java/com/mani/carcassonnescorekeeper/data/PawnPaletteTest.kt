package com.mani.carcassonnescorekeeper.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PawnPaletteTest {
    @Test
    fun presetsAreTheSixPawnColoursInOrder() {
        assertEquals(
            listOf(PawnPalette.YELLOW, PawnPalette.GREEN, PawnPalette.BLUE, PawnPalette.BLACK, PawnPalette.RED, PawnPalette.PINK),
            PawnPalette.presets,
        )
    }

    @Test
    fun hexRoundTrips() {
        val argb = 0xFFAB12CD.toInt()
        assertEquals("#AB12CD", PawnPalette.toHex(argb))
        assertEquals(argb, PawnPalette.parseHex("#AB12CD"))
        assertEquals(argb, PawnPalette.parseHex("ab12cd"))
    }

    @Test
    fun invalidHexIsRejected() {
        assertNull(PawnPalette.parseHex("#AB12C"))
        assertNull(PawnPalette.parseHex("#GG0000"))
        assertNull(PawnPalette.parseHex(""))
    }

    @Test
    fun firstFreeSkipsTakenColoursThenUsesExtras() {
        assertEquals(PawnPalette.YELLOW, PawnPalette.firstFree(emptySet()))
        assertEquals(PawnPalette.BLUE, PawnPalette.firstFree(setOf(PawnPalette.YELLOW, PawnPalette.GREEN)))
        assertEquals(PawnPalette.extras[0], PawnPalette.firstFree(PawnPalette.presets))
    }
}
