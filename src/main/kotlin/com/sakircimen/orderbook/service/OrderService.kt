package com.sakircimen.orderbook.service

import com.sakircimen.orderbook.domain.Currency
import com.sakircimen.orderbook.domain.Order
import com.sakircimen.orderbook.domain.Side
import com.sakircimen.orderbook.utils.currencyPairToString
import com.sakircimen.orderbook.utils.parseCurrencyPair
import com.sakircimen.orderbook.utils.reverse
import com.sakircimen.orderbook.web.OrderBookResponse
import com.sakircimen.orderbook.web.OrderRequest
import com.sakircimen.orderbook.web.OrderResponse
import com.sakircimen.orderbook.web.mapOrderToOrderResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.util.logging.Logger

class OrderService(
    private val tradeService: TradeService,
) {
    private val logger = Logger.getLogger(OrderService::class.java.name)

    companion object {

        private val orderBook: MutableMap<Pair<Currency, Currency>, MutableList<Order>> = mutableMapOf()
        private var orderSequence: Long = 0

        fun reduceMatchingOrders(
            currencyPairToMatchOrders: Pair<Currency, Currency>,
            candidate: Order,
            tradingQuantity: BigDecimal,
        ) {
            val candidateIndex = orderBook[currencyPairToMatchOrders]!!.indexOfFirst { it.id == candidate.id }
            if (tradingQuantity < candidate.quantity) {
                orderBook[currencyPairToMatchOrders]!![candidateIndex] = candidate.copy(
                    quantity = candidate.quantity - tradingQuantity
                )
            } else {
                orderBook[currencyPairToMatchOrders]!!.removeAt(candidateIndex)
            }
        }
    }

    fun listOrders(currencyStr: String): OrderBookResponse {
        logger.info("Listing orders for $currencyStr")

        val requestedCurrencyPair = parseCurrencyPair(currencyStr)
        val sellingCurrencyPair = requestedCurrencyPair.reverse()

        val bids = orderBook[requestedCurrencyPair]?.map { order ->
            OrderResponse(
                side = Side.BUY,
                quantity = order.quantity,
                price = order.effectivePrice,
                currencyPair = currencyPairToString(requestedCurrencyPair),
                orderCount = 1,
            )
        } ?: emptyList()
        val asks = orderBook[sellingCurrencyPair]?.map { order: Order ->
            mapOrderToOrderResponse(order, requestedCurrencyPair)
        } ?: emptyList()
        logger.info("Found ${asks.size} asks and ${bids.size} bids")
        return OrderBookResponse(
            Asks = asks,
            Bids = bids,
            LastChange = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            SequenceNumber = orderSequence,
        )
    }

    fun placeOrder(orderRequest: OrderRequest): String {
        val requestedCurrencyPair = parseCurrencyPair(currencyStr = orderRequest.pair)

        logger.info("Received ${orderRequest.side} request for $requestedCurrencyPair")

        val effectiveCurrencyPair: Pair<Currency, Currency>
        val effectivePrice: BigDecimal
        if (orderRequest.side == Side.BUY) {
            effectiveCurrencyPair = requestedCurrencyPair
            effectivePrice = orderRequest.price
        } else {
            /*
            * SELL order is a BUY order if you reverse currencies
            * e.g. sell BTC for USD == buying USD with BTC
            */
            effectiveCurrencyPair = requestedCurrencyPair.reverse()
            effectivePrice = orderRequest.price.reverse()
        }

        val currencyPairToMatchOrders =
            /*
            * to match orders, we should find orders that are buying currency that you are trading in.
            * e.g. to buy BTC for USD -> matching orders are buying USD for BTC
            */
            effectiveCurrencyPair.reverse()

        val currencyPriceToMatchOrders =
            /*
            * since we reversed currency pair, we should also reverse the currency price.
            */
            effectivePrice.reverse()

        val previousOrders = orderBook[currencyPairToMatchOrders]
        val matchingCandidates = tradeService.findTradableOrders(
            previousOrders = previousOrders,
            requestPrice = currencyPriceToMatchOrders
        ).toMutableList()

        val remainingOrderQuantity = tradeService.tradeMatchingOrdersAndReduceQuantity(
            matchingCandidates = matchingCandidates,
            orderRequest = orderRequest,
            requestedCurrencyPair = requestedCurrencyPair,
            currencyPairToMatchOrders = currencyPairToMatchOrders
        )

        if (remainingOrderQuantity <= BigDecimal.ZERO) {
            logger.info("Requested ${orderRequest.side} request for $requestedCurrencyPair has TAKEN previous deals")
            return "Order matched with previous deals"
        }

        val orderToCreate = Order(
            side = Side.BUY,
            quantity = remainingOrderQuantity,
            originalPrice = orderRequest.price,
            effectivePrice = effectivePrice,
            originalCurrencyPair = requestedCurrencyPair,
            effectiveCurrencyPair = effectiveCurrencyPair,
            currencyPairReversed = requestedCurrencyPair != effectiveCurrencyPair,
        )

        logger.info("Placed BUY order for $effectiveCurrencyPair for an amount of $remainingOrderQuantity")
        val comparingOrders = orderBook[effectiveCurrencyPair]
        if (comparingOrders == null) {
            orderBook[effectiveCurrencyPair] = mutableListOf(orderToCreate)
        } else {
            val indexOfFirstLowerPrice = comparingOrders.withIndex().firstOrNull { (_, existingOrder) ->
                orderToCreate.effectivePrice > existingOrder.effectivePrice
            }?.index ?: comparingOrders.size
            comparingOrders.add(indexOfFirstLowerPrice, orderToCreate)
        }
        orderSequence++

        return "Order created."
    }

    /**
     * Clean the order book for test purposes
     */
    fun refreshOrderBook() {
        orderBook.clear()
        orderSequence = 0
    }
}
