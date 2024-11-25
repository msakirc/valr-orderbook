package com.sakircimen.orderbook.service

import com.sakircimen.orderbook.domain.Side
import com.sakircimen.orderbook.web.OrderRequest
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderServiceIntegrationTest {

    private val orderService = OrderService(TradeService())

    @BeforeTest
    fun cleanup() {
        orderService.refreshOrderBook()
    }

    @Test
    fun `test list orders when empty`() {
        val result = orderService.listOrders("BTCZAR")
        assertEquals(0, result.Asks.size)
        assertEquals(0, result.Bids.size)
        assertEquals(0, result.SequenceNumber)
    }

    @Test
    fun `test add single buy order and list for same currency pair`() {
        val currencyStr = "BTCZAR"
        val orderRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(1.9),
            price = BigDecimal.valueOf(2.3),
            pair = currencyStr
        )
        orderService.placeOrder(orderRequest)

        val orderList = orderService.listOrders(currencyStr)
        assertEquals(0, orderList.Asks.size)
        assertEquals(1, orderList.Bids.size)
        assertEquals(1, orderList.Bids[0].orderCount)
        assertEquals(Side.BUY, orderList.Bids[0].side)
        assertEquals(BigDecimal.valueOf(1.9), orderList.Bids[0].quantity)
        assertEquals(BigDecimal.valueOf(2.3), orderList.Bids[0].price)
        assertEquals(currencyStr, orderList.Bids[0].currencyPair)
    }

    @Test
    fun `test add single buy order and list for reverse currency pair`() {
        val currencyStr = "BTCZAR"
        val reverseCurrencyStr = "ZARBTC"
        val orderRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(1.9),
            price = BigDecimal.valueOf(2.3),
            pair = currencyStr
        )
        orderService.placeOrder(orderRequest)

        val orderList = orderService.listOrders(reverseCurrencyStr)
        assertEquals(1, orderList.Asks.size)
        assertEquals(0, orderList.Bids.size)
        assertEquals(1, orderList.Asks[0].orderCount)
        assertEquals(Side.SELL, orderList.Asks[0].side)
        assertEquals(BigDecimal.valueOf(1.9), orderList.Asks[0].quantity)
        assertEquals(BigDecimal.valueOf(2.3), orderList.Asks[0].price)
        assertEquals(reverseCurrencyStr, orderList.Asks[0].currencyPair)
    }

    @Test
    fun `test add single sell order and list for same currency pair`() {
        val currencyStr = "BTCZAR"
        val orderRequest = OrderRequest(
            side = Side.SELL,
            quantity = BigDecimal.valueOf(29.10),
            price = BigDecimal.valueOf(192.3),
            pair = currencyStr
        )
        orderService.placeOrder(orderRequest)

        val orderList = orderService.listOrders(currencyStr)
        assertEquals(1, orderList.Asks.size)
        assertEquals(0, orderList.Bids.size)
        assertEquals(1, orderList.Asks[0].orderCount)
        assertEquals(Side.SELL, orderList.Asks[0].side)
        assertEquals(BigDecimal.valueOf(29.1), orderList.Asks[0].quantity)
        assertEquals(BigDecimal.valueOf(192.3), orderList.Asks[0].price)
        assertEquals(currencyStr, orderList.Asks[0].currencyPair)
    }

    @Test
    fun `test add single sell order and list for reverse currency pair`() {
        val currencyStr = "BTCZAR"
        val reverseCurrencyStr = "ZARBTC"
        val orderRequest = OrderRequest(
            side = Side.SELL,
            quantity = BigDecimal.valueOf(1.9),
            price = BigDecimal.valueOf(2.3),
            pair = currencyStr
        )
        orderService.placeOrder(orderRequest)

        val orderList = orderService.listOrders(reverseCurrencyStr)
        assertEquals(0, orderList.Asks.size)
        assertEquals(1, orderList.Bids.size)
        assertEquals(1, orderList.Bids[0].orderCount)
        assertEquals(Side.BUY, orderList.Bids[0].side)
        assertEquals(BigDecimal.valueOf(1.9), orderList.Bids[0].quantity)
        assertEquals(
            BigDecimal.valueOf(0.4347826086956522).setScale(10, RoundingMode.HALF_EVEN),
            orderList.Bids[0].price.setScale(10, RoundingMode.HALF_EVEN)
        )
        assertEquals(reverseCurrencyStr, orderList.Bids[0].currencyPair)
    }

    @Ignore
    @Test
    fun `test add sell and buy orders for same currency pair and list`() {
        val currencyStr = "BTCZAR"
        val buyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(17.05),
            price = BigDecimal.valueOf(2000.0),
            pair = currencyStr
        )
        orderService.placeOrder(buyRequest)

        val sellRequest = OrderRequest(
            side = Side.SELL,
            quantity = BigDecimal.valueOf(30.8),
            price = BigDecimal.valueOf(1922.0),
            pair = currencyStr
        )
        orderService.placeOrder(sellRequest)

        val orderList = orderService.listOrders(currencyStr)

        assertEquals(1, orderList.Bids.size)
        assertEquals(1, orderList.Bids[0].orderCount)
        assertEquals(Side.BUY, orderList.Bids[0].side)
        assertEquals(BigDecimal.valueOf(17.05), orderList.Bids[0].quantity)
        assertEquals(BigDecimal.valueOf(2000.0), orderList.Bids[0].price)
        assertEquals(currencyStr, orderList.Bids[0].currencyPair)

        assertEquals(1, orderList.Asks.size)
        assertEquals(1, orderList.Asks[0].orderCount)
        assertEquals(Side.SELL, orderList.Asks[0].side)
        assertEquals(BigDecimal.valueOf(30.8), orderList.Asks[0].quantity)
        assertEquals(BigDecimal.valueOf(1922.0), orderList.Asks[0].price)
        assertEquals(currencyStr, orderList.Asks[0].currencyPair)
    }

    @Test
    fun `test add multiple buy orders and list orders in price order`() {
        val currencyStr = "BTCZAR"
        listOf(1071, 1922, 1040, 1923, 1920, 1517, 1453, 1974, 1025, 2000).forEach { index ->
            val buyRequest = OrderRequest(
                side = Side.BUY,
                quantity = BigDecimal.valueOf("$index.$index".toDouble()),
                price = BigDecimal.valueOf(index.toDouble()),
                pair = currencyStr
            )
            orderService.placeOrder(buyRequest)
        }
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(10, orderList.Bids.size)

        listOf(2000, 1974, 1923, 1922, 1920, 1517, 1453, 1071, 1040, 1025).forEachIndexed { index, element ->
            assertEquals(Side.BUY, orderList.Bids[index].side)
            assertEquals(1, orderList.Bids[index].orderCount)
            assertEquals(BigDecimal.valueOf("$element.$element".toDouble()), orderList.Bids[index].quantity)
            assertEquals(BigDecimal.valueOf(element.toDouble()), orderList.Bids[index].price)
            assertEquals(currencyStr, orderList.Bids[index].currencyPair)
        }
    }

    @Test
    fun `test add multiple sell orders and list orders in price order`() {
        val currencyStr = "BTCZAR"
        listOf(15, 23, 8, 4, 42, 16).forEach { index ->
            val buyRequest = OrderRequest(
                side = Side.SELL,
                quantity = BigDecimal.valueOf("$index.$index".toDouble()),
                price = BigDecimal.valueOf(index.toDouble()),
                pair = currencyStr
            )
            orderService.placeOrder(buyRequest)
        }
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(6, orderList.Asks.size)
        assertEquals(0, orderList.Bids.size)

        listOf(4, 8, 15, 16, 23, 42).forEachIndexed { index, element ->
            assertEquals(Side.SELL, orderList.Asks[index].side)
            assertEquals(1, orderList.Asks[index].orderCount)
            assertEquals(BigDecimal.valueOf("$element.$element".toDouble()), orderList.Asks[index].quantity)
            assertEquals(BigDecimal.valueOf(element.toDouble()), orderList.Asks[index].price)
            assertEquals(currencyStr, orderList.Asks[index].currencyPair)
        }
    }

    @Test
    fun `test add multiple buy and sell orders and list orders in price order`() {
        val currencyStr = "BTCZAR"
        (0..10).forEach { index ->
            val buyRequest = OrderRequest(
                side = Side.BUY,
                quantity = BigDecimal.valueOf(10 + "$index.$index".toDouble()),
                price = BigDecimal.valueOf(10 + index.toDouble()),
                pair = currencyStr
            )
            orderService.placeOrder(buyRequest)

            val sellRequest = OrderRequest(
                side = Side.SELL,
                quantity = BigDecimal.valueOf(500 + index.toDouble()),
                price = BigDecimal.valueOf(500 + index.toDouble()),
                pair = currencyStr
            )
            orderService.placeOrder(sellRequest)
        }

        val orderList = orderService.listOrders(currencyStr)

        assertEquals(11, orderList.Bids.size)
        assertEquals(11, orderList.Asks.size)

        (0..10).forEach { index ->
            val reverseIndex = 10 - index
            assertEquals(Side.BUY, orderList.Bids[index].side)
            assertEquals(1, orderList.Bids[index].orderCount)
            assertEquals(
                BigDecimal.valueOf(10 + "$reverseIndex.$reverseIndex".toDouble()),
                orderList.Bids[index].quantity
            )
            assertEquals(BigDecimal.valueOf(10 + reverseIndex.toDouble()), orderList.Bids[index].price)
            assertEquals(currencyStr, orderList.Bids[index].currencyPair)

            assertEquals(Side.SELL, orderList.Asks[index].side)
            assertEquals(1, orderList.Asks[index].orderCount)
            assertEquals(BigDecimal.valueOf(500 + index.toDouble()), orderList.Asks[index].quantity)
            assertEquals(BigDecimal.valueOf(500 + index.toDouble()), orderList.Asks[index].price)
            assertEquals(currencyStr, orderList.Asks[index].currencyPair)
        }
    }

    @Ignore
    @Test
    fun `test put buy and sell orders on same currencies but no trades due to different quantities`() {
        val currencyStr = "BTCZAR"
        val reverseCurrencyStr = "ZARBTC"
        val buyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(17.05),
            price = BigDecimal.valueOf(1905.0),
            pair = currencyStr
        )
        orderService.placeOrder(buyRequest)

        val sellRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(2000.0),
            price = BigDecimal.valueOf(1905.0),
            pair = reverseCurrencyStr
        )
        orderService.placeOrder(sellRequest)

        val orderList = orderService.listOrders(currencyStr)

        assertEquals(1, orderList.Bids.size)
        assertEquals(1, orderList.Bids[0].orderCount)
        assertEquals(Side.BUY, orderList.Bids[0].side)
        assertEquals(BigDecimal.valueOf(17.05), orderList.Bids[0].quantity)
        assertEquals(BigDecimal.valueOf(1905.0), orderList.Bids[0].price)
        assertEquals(currencyStr, orderList.Bids[0].currencyPair)

        assertEquals(1, orderList.Asks.size)
        assertEquals(1, orderList.Asks[0].orderCount)
        assertEquals(Side.SELL, orderList.Asks[0].side)
        assertEquals(BigDecimal.valueOf(2000.0), orderList.Asks[0].quantity)
        assertEquals(BigDecimal.valueOf(1905.0), orderList.Asks[0].price)
        assertEquals(currencyStr, orderList.Asks[0].currencyPair)
    }

    @Test
    fun `test put buy and sell orders on same currencies but no trades due to different prices`() {
        val currencyStr = "BTCZAR"
        val reverseCurrencyStr = "ZARBTC"
        val buyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(2000.0),
            price = BigDecimal.valueOf(0.5),
            pair = currencyStr
        )
        orderService.placeOrder(buyRequest)

        val sellRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(1905.0),
            price = BigDecimal.valueOf(1.9),
            pair = reverseCurrencyStr
        )
        orderService.placeOrder(sellRequest)

        val orderList = orderService.listOrders(currencyStr)

        assertEquals(1, orderList.Bids.size)
        assertEquals(1, orderList.Bids[0].orderCount)
        assertEquals(Side.BUY, orderList.Bids[0].side)
        assertEquals(BigDecimal.valueOf(0.5), orderList.Bids[0].price)
        assertEquals(BigDecimal.valueOf(2000.0), orderList.Bids[0].quantity)
        assertEquals(currencyStr, orderList.Bids[0].currencyPair)

        assertEquals(1, orderList.Asks.size)
        assertEquals(1, orderList.Asks[0].orderCount)
        assertEquals(Side.SELL, orderList.Asks[0].side)
        assertEquals(BigDecimal.valueOf(1.9), orderList.Asks[0].price)
        assertEquals(BigDecimal.valueOf(1905.0), orderList.Asks[0].quantity)
        assertEquals(currencyStr, orderList.Asks[0].currencyPair)
    }
}
