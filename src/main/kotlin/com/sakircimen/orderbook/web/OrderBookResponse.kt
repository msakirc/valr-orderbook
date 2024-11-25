package com.sakircimen.orderbook.web

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class OrderBookResponse(
    val Asks: List<OrderResponse>,
    val Bids: List<OrderResponse>,
    val LastChange: LocalDateTime,
    val SequenceNumber: Long,
)
