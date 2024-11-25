package com.sakircimen.orderbook.web

import com.sakircimen.orderbook.domain.Currency
import com.sakircimen.orderbook.domain.Order
import com.sakircimen.orderbook.domain.Side
import com.sakircimen.orderbook.domain.Trade
import com.sakircimen.orderbook.utils.currencyPairToString

fun mapOrderToOrderResponse(
    order: Order,
    currencyPair: Pair<Currency, Currency>,
) = OrderResponse(
    side = Side.SELL,
    quantity = order.quantity,
    price = if (order.currencyPairReversed) order.originalPrice else order.effectivePrice,
    currencyPair = currencyPairToString(currencyPair),
    orderCount = 1,
)

fun mapTradeToTradeResponse(trade: Trade): TradeResponse = TradeResponse(
    price = trade.price,
    quantity = trade.quantity,
    currencyPair = currencyPairToString(trade.currencyPair),
    tradedAt = trade.tradedAt,
    takerSide = trade.takerSide,
    sequenceId = trade.sequenceId,
    id = trade.id.toString(),
    quoteVolume = trade.quoteVolume,
)
