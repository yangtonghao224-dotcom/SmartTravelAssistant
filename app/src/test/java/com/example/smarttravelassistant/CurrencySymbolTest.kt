package com.example.smarttravelassistant

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencySymbolTest {

    @Test
    fun testCurrencySymbols() {
        assertEquals("$", currencySymbol("SGD"))
        assertEquals("$", currencySymbol("USD"))
        assertEquals("¥", currencySymbol("CNY"))
        assertEquals("€", currencySymbol("EUR"))
        assertEquals("£", currencySymbol("GBP"))
        assertEquals("RM", currencySymbol("MYR"))
        assertEquals("₩", currencySymbol("KRW"))
        assertEquals("NT$", currencySymbol("TWD"))
    }
}
