package com.lpi.budgy.valuation

import com.lpi.budgy.currency.CurrencyConverter
import com.lpi.budgy.domain.*
import com.lpi.budgy.stock.StockApi
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class ValuationServiceTest {

    private val currencyConverter = mockk<CurrencyConverter>()
    private val stockApi = mockk<StockApi>()

    private val valuationService = ValuationService(currencyConverter, stockApi)

    @Test
    fun `should calculate value for MonetaryBalance`() {
        val expectedResult = 2.5
        every { currencyConverter.convert(any(), any(), any(), any()) } returns expectedResult

        val balance = MonetaryBalance(
            asset = Account(Institution("1", "test institution"), "test account", Currency("usd", "USD")),
            value = 10.0
        )

        val result = valuationService.value(balance, Currency("pln", "PLN"), LocalDate.of(2020, 10, 5))

        result shouldBe expectedResult

    }

    @Test
    fun `should calculate value for StockBalance`() {
        val abcValuation = 2.0
        val abcAmount = 10.0
        val xyzValuation = 1.0
        val xyzAmount = 20.0
        every { stockApi.value("ABC", any(), any()) } returns abcValuation
        every { stockApi.value("XYZ", any(), any()) } returns xyzValuation

        val balance = StocksBalance(
            asset = Account(Institution("1", "test institution"), "test account", Currency("usd", "USD")),
            stocksAmounts = mapOf("ABC" to abcAmount, "XYZ" to xyzAmount),
            isCrypto = false
        )

        val result = valuationService.value(balance, Currency("pln", "PLN"), LocalDate.of(2020, 10, 5))

        result shouldBe abcValuation * 10.0 + xyzValuation * 20.0

    }
}