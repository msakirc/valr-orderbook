package com.sakircimen.orderbook.web

import com.sakircimen.orderbook.domain.BigDecimalJson
import com.sakircimen.orderbook.domain.Side
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class TradeResponse(
    val price: BigDecimalJson,
    val quantity: BigDecimalJson,
    val currencyPair: String,
    val tradedAt: LocalDateTime,
    val takerSide: Side,
    val sequenceId: Int,

    val id: String,
    val quoteVolume: BigDecimalJson,
)
