package com.sakircimen.orderbook.web

import com.sakircimen.orderbook.domain.Side
import com.sakircimen.orderbook.domain.BigDecimalJson
import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    val side: Side,
    val quantity: BigDecimalJson,
    val price: BigDecimalJson,
    val currencyPair: String,
    val orderCount: Int,
)
