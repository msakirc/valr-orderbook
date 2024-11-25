package com.sakircimen.orderbook.service

import com.sakircimen.orderbook.domain.Side
import com.sakircimen.orderbook.web.OrderRequest
import java.math.BigDecimal
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class TradeServiceIntegrationTest {

    private val tradeService: TradeService = TradeService()
    private var orderService: OrderService = OrderService(tradeService)

    @BeforeTest
    fun cleanup() {
        orderService.refreshOrderBook()
        tradeService.refreshTrades()
    }

    @Test
    fun `test make a trade with no remaining orders and taker side is SELL`() {
        val currencyStr = "BTCZAR"
        val buyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(1905),
            price = BigDecimal.valueOf(2024),
            pair = currencyStr
        )
        orderService.placeOrder(buyRequest)

        assertEquals(0, tradeService.getTradeHistory(currencyStr).size)
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(0, orderList.Asks.size)
        assertEquals(1, orderList.Bids.size)
        assertEquals(1, orderList.Bids[0].orderCount)
        assertEquals(Side.BUY, orderList.Bids[0].side)
        assertEquals(BigDecimal.valueOf(2024), orderList.Bids[0].price)
        assertEquals(BigDecimal.valueOf(1905), orderList.Bids[0].quantity)
        assertEquals(currencyStr, orderList.Bids[0].currencyPair)

        val sellRequest = OrderRequest(
            side = Side.SELL,
            quantity = BigDecimal.valueOf(1905),
            price = BigDecimal.valueOf(2000),
            pair = currencyStr
        )
        orderService.placeOrder(sellRequest)

        val orderListAfterTrade = orderService.listOrders(currencyStr)
        assertEquals(0, orderListAfterTrade.Asks.size)
        assertEquals(0, orderListAfterTrade.Bids.size)

        val tradeHistory = tradeService.getTradeHistory(currencyStr)
        assertEquals(1, tradeHistory.size)
        assertEquals(Side.SELL, tradeHistory[0].takerSide)
        assertEquals(BigDecimal.valueOf(1905), tradeHistory[0].quantity)
        assertEquals(BigDecimal.valueOf(2000), tradeHistory[0].price)
        assertEquals(currencyStr, tradeHistory[0].currencyPair)
        assertEquals(0, tradeHistory[0].sequenceId)
        assertEquals(BigDecimal.valueOf(1905 * 2000), tradeHistory[0].quoteVolume)
    }

    @Test
    fun `test make a trade with taker is SELL and quantity bigger than maker`() {
        val currencyStr = "BTCZAR"
        val buyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(17.05),
            price = BigDecimal.valueOf(2000),
            pair = currencyStr
        )
        orderService.placeOrder(buyRequest)

        assertEquals(0, tradeService.getTradeHistory(currencyStr).size)
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(0, orderList.Asks.size)
        assertEquals(1, orderList.Bids.size)
        assertEquals(1, orderList.Bids[0].orderCount)
        assertEquals(Side.BUY, orderList.Bids[0].side)
        assertEquals(BigDecimal.valueOf(17.05), orderList.Bids[0].quantity)
        assertEquals(BigDecimal.valueOf(2000), orderList.Bids[0].price)
        assertEquals(currencyStr, orderList.Bids[0].currencyPair)

        val sellRequest = OrderRequest(
            side = Side.SELL,
            quantity = BigDecimal.valueOf(19),
            price = BigDecimal.valueOf(1905),
            pair = currencyStr
        )
        orderService.placeOrder(sellRequest)

        val orderListAfterTrade = orderService.listOrders(currencyStr)
        assertEquals(0, orderListAfterTrade.Bids.size)
        assertEquals(1, orderListAfterTrade.Asks.size)
        assertEquals(1, orderListAfterTrade.Asks[0].orderCount)
        assertEquals(Side.SELL, orderListAfterTrade.Asks[0].side)
        assertEquals(BigDecimal.valueOf(1.95), orderListAfterTrade.Asks[0].quantity)
        assertEquals(BigDecimal.valueOf(1905), orderListAfterTrade.Asks[0].price)
        assertEquals(currencyStr, orderListAfterTrade.Asks[0].currencyPair)

        val tradeHistory = tradeService.getTradeHistory(currencyStr)
        assertEquals(1, tradeHistory.size)
        assertEquals(Side.SELL, tradeHistory[0].takerSide)
        assertEquals(BigDecimal.valueOf(17.05), tradeHistory[0].quantity)
        assertEquals(BigDecimal.valueOf(1905), tradeHistory[0].price)
        assertEquals(currencyStr, tradeHistory[0].currencyPair)
        assertEquals(0, tradeHistory[0].sequenceId)
        assertEquals(BigDecimal.valueOf(17.05 * 1905), tradeHistory[0].quoteVolume)
    }

    @Test
    fun `test make a trade with taker is SELL and quantity less than maker`() {
        val currencyStr = "BTCZAR"
        val buyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(1905),
            price = BigDecimal.valueOf(2000),
            pair = currencyStr
        )
        orderService.placeOrder(buyRequest)

        assertEquals(0, tradeService.getTradeHistory(currencyStr).size)
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(0, orderList.Asks.size)
        assertEquals(1, orderList.Bids.size)
        assertEquals(1, orderList.Bids[0].orderCount)
        assertEquals(Side.BUY, orderList.Bids[0].side)
        assertEquals(BigDecimal.valueOf(1905), orderList.Bids[0].quantity)
        assertEquals(BigDecimal.valueOf(2000), orderList.Bids[0].price)
        assertEquals(currencyStr, orderList.Bids[0].currencyPair)

        val sellRequest = OrderRequest(
            side = Side.SELL,
            quantity = BigDecimal.valueOf(17.05),
            price = BigDecimal.valueOf(1905),
            pair = currencyStr
        )
        orderService.placeOrder(sellRequest)

        val orderListAfterTrade = orderService.listOrders(currencyStr)
        assertEquals(0, orderListAfterTrade.Asks.size)
        assertEquals(1, orderListAfterTrade.Bids.size)
        assertEquals(1, orderListAfterTrade.Bids[0].orderCount)
        assertEquals(Side.BUY, orderListAfterTrade.Bids[0].side)
        assertEquals(BigDecimal.valueOf(1887.95), orderListAfterTrade.Bids[0].quantity)
        assertEquals(BigDecimal.valueOf(2000), orderListAfterTrade.Bids[0].price)
        assertEquals(currencyStr, orderListAfterTrade.Bids[0].currencyPair)

        val tradeHistory = tradeService.getTradeHistory(currencyStr)
        assertEquals(1, tradeHistory.size)
        assertEquals(Side.SELL, tradeHistory[0].takerSide)
        assertEquals(BigDecimal.valueOf(17.05), tradeHistory[0].quantity)
        assertEquals(BigDecimal.valueOf(1905), tradeHistory[0].price)
        assertEquals(currencyStr, tradeHistory[0].currencyPair)
        assertEquals(0, tradeHistory[0].sequenceId)
        assertEquals(BigDecimal.valueOf(17.05 * 1905), tradeHistory[0].quoteVolume)
    }

    @Test
    fun `test make multiple trade with taker is SELL and quantity less than total makers`() {
        val currencyStr = "BTCZAR"
        listOf(1905, 2000, 2001L).forEachIndexed { index, element ->
            val buyRequest = OrderRequest(
                side = Side.BUY,
                quantity = BigDecimal.valueOf(5 + "$index.$index".toDouble()),
                price = BigDecimal.valueOf(element),
                pair = currencyStr
            )
            orderService.placeOrder(buyRequest)
        }

        assertEquals(0, tradeService.getTradeHistory(currencyStr).size)
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(0, orderList.Asks.size)
        assertEquals(3, orderList.Bids.size)

        val sellRequest = OrderRequest(
            side = Side.SELL,
            quantity = BigDecimal.valueOf(17.05),
            price = BigDecimal.valueOf(1905),
            pair = currencyStr
        )
        orderService.placeOrder(sellRequest)

        val orderListAfterTrade = orderService.listOrders(currencyStr)
        assertEquals(0, orderListAfterTrade.Asks.size)
        assertEquals(1, orderListAfterTrade.Bids.size)
        assertEquals(1, orderListAfterTrade.Bids[0].orderCount)
        assertEquals(Side.BUY, orderListAfterTrade.Bids[0].side)
        assertEquals(BigDecimal.valueOf(1.25), orderListAfterTrade.Bids[0].quantity)
        assertContains(listOf(1905, 2000, 2001), orderListAfterTrade.Bids[0].price.toLong())

        assertEquals(currencyStr, orderListAfterTrade.Bids[0].currencyPair)

        val tradeHistory = tradeService.getTradeHistory(currencyStr)
        assertEquals(3, tradeHistory.size)
        tradeHistory.forEachIndexed { index, trade ->
            assertEquals(Side.SELL, trade.takerSide)
            assertEquals(currencyStr, trade.currencyPair)
            assertContains(listOf(3.75, 6.1, 7.2), trade.quantity.toDouble())
            assertEquals(BigDecimal.valueOf(1905), trade.price)
            assertContains(listOf(3.75 * 1905, 6.1 * 1905, 7.2 * 1905), trade.quoteVolume.toDouble())
            assertEquals(index, trade.sequenceId)
        }
    }

    @Test
    fun `test make a trade with no remaining orders and taker side is BUY`() {
        val currencyStr = "BTCZAR"
        val sellRequest = OrderRequest(
            side = Side.SELL,
            quantity = BigDecimal.valueOf(1905),
            price = BigDecimal.valueOf(2000),
            pair = currencyStr
        )
        orderService.placeOrder(sellRequest)

        assertEquals(0, tradeService.getTradeHistory(currencyStr).size)
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(0, orderList.Bids.size)
        assertEquals(1, orderList.Asks.size)
        assertEquals(1, orderList.Asks[0].orderCount)
        assertEquals(Side.SELL, orderList.Asks[0].side)
        assertEquals(BigDecimal.valueOf(2000), orderList.Asks[0].price)
        assertEquals(BigDecimal.valueOf(1905), orderList.Asks[0].quantity)
        assertEquals(currencyStr, orderList.Asks[0].currencyPair)

        val buyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(1905),
            price = BigDecimal.valueOf(2024),
            pair = currencyStr
        )
        orderService.placeOrder(buyRequest)

        val orderListAfterTrade = orderService.listOrders(currencyStr)
        assertEquals(0, orderListAfterTrade.Asks.size)
        assertEquals(0, orderListAfterTrade.Bids.size)

        val tradeHistory = tradeService.getTradeHistory(currencyStr)
        assertEquals(1, tradeHistory.size)
        assertEquals(Side.BUY, tradeHistory[0].takerSide)
        assertEquals(BigDecimal.valueOf(1905), tradeHistory[0].quantity)
        assertEquals(BigDecimal.valueOf(2024), tradeHistory[0].price)
        assertEquals(currencyStr, tradeHistory[0].currencyPair)
        assertEquals(0, tradeHistory[0].sequenceId)
        assertEquals(BigDecimal.valueOf(1905 * 2024), tradeHistory[0].quoteVolume)
    }
    @Test
    fun `test make a trade with taker is BUY and quantity bigger than maker`() {
        val currencyStr = "BTCZAR"
        val sellRequest = OrderRequest(
            side = Side.SELL,
            quantity = BigDecimal.valueOf(17.05),
            price = BigDecimal.valueOf(1905),
            pair = currencyStr
        )
        orderService.placeOrder(sellRequest)

        assertEquals(0, tradeService.getTradeHistory(currencyStr).size)
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(0, orderList.Bids.size)
        assertEquals(1, orderList.Asks.size)
        assertEquals(1, orderList.Asks[0].orderCount)
        assertEquals(Side.SELL, orderList.Asks[0].side)
        assertEquals(BigDecimal.valueOf(17.05), orderList.Asks[0].quantity)
        assertEquals(BigDecimal.valueOf(1905), orderList.Asks[0].price)
        assertEquals(currencyStr, orderList.Asks[0].currencyPair)

        val buyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(1905),
            price = BigDecimal.valueOf(2000),
            pair = currencyStr
        )
        orderService.placeOrder(buyRequest)

        val orderListAfterTrade = orderService.listOrders(currencyStr)
        assertEquals(0, orderListAfterTrade.Asks.size)
        assertEquals(1, orderListAfterTrade.Bids.size)
        assertEquals(1, orderListAfterTrade.Bids[0].orderCount)
        assertEquals(Side.BUY, orderListAfterTrade.Bids[0].side)
        assertEquals(BigDecimal.valueOf(1887.95), orderListAfterTrade.Bids[0].quantity)
        assertEquals(BigDecimal.valueOf(2000), orderListAfterTrade.Bids[0].price)
        assertEquals(currencyStr, orderListAfterTrade.Bids[0].currencyPair)

        val tradeHistory = tradeService.getTradeHistory(currencyStr)
        assertEquals(1, tradeHistory.size)
        assertEquals(Side.BUY, tradeHistory[0].takerSide)
        assertEquals(BigDecimal.valueOf(17.05), tradeHistory[0].quantity)
        assertEquals(BigDecimal.valueOf(2000), tradeHistory[0].price)
        assertEquals(currencyStr, tradeHistory[0].currencyPair)
        assertEquals(0, tradeHistory[0].sequenceId)
        assertEquals(BigDecimal.valueOf(17.05 * 2000).setScale(10), tradeHistory[0].quoteVolume.setScale(10))
    }

    @Test
    fun `test make a trade with taker is BUY and quantity less than maker`() {
        val currencyStr = "BTCZAR"
        val sellRequest = OrderRequest(
            side = Side.SELL,
            quantity = BigDecimal.valueOf(19),
            price = BigDecimal.valueOf(1905),
            pair = currencyStr
        )
        orderService.placeOrder(sellRequest)

        assertEquals(0, tradeService.getTradeHistory(currencyStr).size)
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(0, orderList.Bids.size)
        assertEquals(1, orderList.Asks.size)
        assertEquals(1, orderList.Asks[0].orderCount)
        assertEquals(Side.SELL, orderList.Asks[0].side)
        assertEquals(BigDecimal.valueOf(19), orderList.Asks[0].quantity)
        assertEquals(BigDecimal.valueOf(1905), orderList.Asks[0].price)
        assertEquals(currencyStr, orderList.Asks[0].currencyPair)

        val buyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(17.05),
            price = BigDecimal.valueOf(2000),
            pair = currencyStr
        )
        orderService.placeOrder(buyRequest)

        val orderListAfterTrade = orderService.listOrders(currencyStr)
        assertEquals(0, orderListAfterTrade.Bids.size)
        assertEquals(1, orderListAfterTrade.Asks.size)
        assertEquals(1, orderListAfterTrade.Asks[0].orderCount)
        assertEquals(Side.SELL, orderListAfterTrade.Asks[0].side)
        assertEquals(BigDecimal.valueOf(1.95), orderListAfterTrade.Asks[0].quantity)
        assertEquals(BigDecimal.valueOf(1905), orderListAfterTrade.Asks[0].price)
        assertEquals(currencyStr, orderListAfterTrade.Asks[0].currencyPair)

        val tradeHistory = tradeService.getTradeHistory(currencyStr)
        assertEquals(1, tradeHistory.size)
        assertEquals(Side.BUY, tradeHistory[0].takerSide)
        assertEquals(BigDecimal.valueOf(17.05), tradeHistory[0].quantity)
        assertEquals(BigDecimal.valueOf(2000), tradeHistory[0].price)
        assertEquals(currencyStr, tradeHistory[0].currencyPair)
        assertEquals(0, tradeHistory[0].sequenceId)
        assertEquals(BigDecimal.valueOf(17.05 * 2000).setScale(10), tradeHistory[0].quoteVolume.setScale(10))
    }

    @Test
    fun `test make multiple trade with taker is BUY and quantity less than total makers`() {
        val currencyStr = "BTCZAR"
        listOf(1905, 2000, 2001L).forEachIndexed { index, element ->
            val sellRequest = OrderRequest(
                side = Side.SELL,
                quantity = BigDecimal.valueOf(5 + "$index.$index".toDouble()),
                price = BigDecimal.valueOf(element),
                pair = currencyStr
            )
            orderService.placeOrder(sellRequest)
        }

        assertEquals(0, tradeService.getTradeHistory(currencyStr).size)
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(0, orderList.Bids.size)
        assertEquals(3, orderList.Asks.size)

        val sellRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(17.05),
            price = BigDecimal.valueOf(1905),
            pair = currencyStr
        )
        orderService.placeOrder(sellRequest)

        val orderListAfterTrade = orderService.listOrders(currencyStr)
        assertEquals(0, orderListAfterTrade.Bids.size)
        assertEquals(1, orderListAfterTrade.Asks.size)
        assertEquals(1, orderListAfterTrade.Asks[0].orderCount)
        assertEquals(Side.SELL, orderListAfterTrade.Asks[0].side)
        assertEquals(BigDecimal.valueOf(1.25), orderListAfterTrade.Asks[0].quantity)
        assertContains(listOf(1905, 2000, 2001), orderListAfterTrade.Asks[0].price.toLong())
        assertEquals(currencyStr, orderListAfterTrade.Asks[0].currencyPair)

        val tradeHistory = tradeService.getTradeHistory(currencyStr)
        assertEquals(3, tradeHistory.size)
        tradeHistory.forEachIndexed { index, trade ->
            assertEquals(Side.BUY, trade.takerSide)
            assertEquals(currencyStr, trade.currencyPair)
            assertContains(listOf(5.0, 5.95, 6.1), trade.quantity.toDouble())
            assertEquals(BigDecimal.valueOf(1905), trade.price)
            assertContains(listOf(5.0 * 1905, 5.95 * 1905, 6.1 * 1905), trade.quoteVolume.toDouble())
            assertEquals(index, trade.sequenceId)
        }
    }

    @Test
    fun `test make a trade with two inverse BUY orders`() {
        val currencyStr = "BTCZAR"
        val reverseCurrencyStr = "ZARBTC"
        val buyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(1905),
            price = BigDecimal.valueOf(2024),
            pair = currencyStr
        )
        orderService.placeOrder(buyRequest)

        assertEquals(0, tradeService.getTradeHistory(currencyStr).size)
        val orderList = orderService.listOrders(currencyStr)

        assertEquals(0, orderList.Asks.size)
        assertEquals(1, orderList.Bids.size)
        assertEquals(1, orderList.Bids[0].orderCount)
        assertEquals(Side.BUY, orderList.Bids[0].side)
        assertEquals(BigDecimal.valueOf(2024), orderList.Bids[0].price)
        assertEquals(BigDecimal.valueOf(1905), orderList.Bids[0].quantity)
        assertEquals(currencyStr, orderList.Bids[0].currencyPair)

        val inverseBuyRequest = OrderRequest(
            side = Side.BUY,
            quantity = BigDecimal.valueOf(1905),
            price = BigDecimal.valueOf(0.0006),
            pair = reverseCurrencyStr
        )
        orderService.placeOrder(inverseBuyRequest)

        val orderListAfterTrade = orderService.listOrders(currencyStr)
        assertEquals(0, orderListAfterTrade.Asks.size)
        assertEquals(0, orderListAfterTrade.Bids.size)

        val tradeHistory = tradeService.getTradeHistory(currencyStr)
        assertEquals(1, tradeHistory.size)
        assertEquals(Side.BUY, tradeHistory[0].takerSide)
        assertEquals(BigDecimal.valueOf(1905), tradeHistory[0].quantity)
        assertEquals(BigDecimal.valueOf(0.0006).setScale(10), tradeHistory[0].price.setScale(10))
        assertEquals(reverseCurrencyStr, tradeHistory[0].currencyPair)
        assertEquals(0, tradeHistory[0].sequenceId)
        assertEquals(BigDecimal.valueOf(1905).multiply( BigDecimal.valueOf(0.0006)), tradeHistory[0].quoteVolume)
    }
}
