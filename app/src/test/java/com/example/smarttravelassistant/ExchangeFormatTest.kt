package com.example.smarttravelassistant

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ExchangeFormatTest {

    @Test
    fun rate_isFormattedTo3DecimalPlaces() {
        val rate = 5.43287
        val formatted = String.format(Locale.US, "%.3f", rate)
        assertEquals("5.433", formatted)
    }
}
