package com.sakircimen.orderbook.service

import com.sakircimen.orderbook.domain.Currency
import com.sakircimen.orderbook.domain.Order
import com.sakircimen.orderbook.domain.Trade
import com.sakircimen.orderbook.exception.InvalidPaginationException
import com.sakircimen.orderbook.utils.parseCurrencyPair
import com.sakircimen.orderbook.utils.reverse
import com.sakircimen.orderbook.web.OrderRequest
import com.sakircimen.orderbook.web.TradeResponse
import com.sakircimen.orderbook.web.mapTradeToTradeResponse
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.util.logging.Logger
import kotlin.math.min

class TradeService {

    private val logger = Logger.getLogger(TradeService::class.java.name)

    companion object {

        private val previousTrades: MutableMap<Pair<Currency, Currency>, MutableList<Trade>> = mutableMapOf()
        private var tradeSequence: Long = 0
        private var lastTrade = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    fun findTradableOrders(
        previousOrders: MutableList<Order>?,
        requestPrice: BigDecimal,
    ): List<Order> {
        if (previousOrders.isNullOrEmpty())
            return emptyList()

        val indexOfFirstMatchingOrders = previousOrders.withIndex().firstOrNull { (_, value) ->
            value.effectivePrice >= requestPrice
        }?.index

        return indexOfFirstMatchingOrders?.let {
            previousOrders.drop(it)
        } ?: emptyList()
    }

    fun tradeMatchingOrdersAndReduceQuantity(
        matchingCandidates: MutableList<Order>,
        orderRequest: OrderRequest,
        requestedCurrencyPair: Pair<Currency, Currency>,
        currencyPairToMatchOrders: Pair<Currency, Currency>,
    ): BigDecimal {
        if (previousTrades[requestedCurrencyPair] == null)
            previousTrades[requestedCurrencyPair] = mutableListOf()

        var remainingOrderQuantity = orderRequest.quantity

        matchingCandidates.forEach { candidate ->
            val tradingQuantity = remainingOrderQuantity.min(candidate.quantity)

            val trade = Trade(
                price = orderRequest.price,
                quantity = tradingQuantity,
                currencyPair = requestedCurrencyPair,
                takerSide = orderRequest.side,
                sequenceId = tradeSequence++.toInt(),
                quoteVolume = orderRequest.price * tradingQuantity
            )
            previousTrades[requestedCurrencyPair]!!.add(trade)
            lastTrade = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            logger.info(
                "Trading request of ${orderRequest.side} ${orderRequest.quantity} ${requestedCurrencyPair} " +
                    "with existing order ${candidate.id} for $tradingQuantity"
            )

            remainingOrderQuantity -= tradingQuantity
            OrderService.reduceMatchingOrders(
                currencyPairToMatchOrders = currencyPairToMatchOrders,
                candidate = candidate,
                tradingQuantity = tradingQuantity
            )

            if (remainingOrderQuantity <= BigDecimal.ZERO) return BigDecimal.ZERO
        }
        return remainingOrderQuantity
    }

    fun getTradeHistory(
        currencyStr: String,
        offset: Int = 0,
        limit: Int = 10,
    ): List<TradeResponse> {
        val currencyPair = parseCurrencyPair(currencyStr)
        val relevantTrades = previousTrades[currencyPair] ?: emptyList()
        val reverseRelevantTrades = previousTrades[currencyPair.reverse()] ?: emptyList()
        val allRelevantTrades = relevantTrades + reverseRelevantTrades

        if (offset > allRelevantTrades.size) {
            throw InvalidPaginationException(allRelevantTrades.size / limit + 1)
        }

        return allRelevantTrades.subList(offset, min(limit + offset, allRelevantTrades.size)).map {
            mapTradeToTradeResponse(it)
        }
    }

    /**
     * Clean the order book for test purposes
     */
    fun refreshTrades() {
        previousTrades.clear()
        tradeSequence = 0
    }
}
