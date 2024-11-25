package com.sakircimen.orderbook.web

import com.sakircimen.orderbook.domain.BigDecimalJson
import com.sakircimen.orderbook.domain.Side
import kotlinx.serialization.Serializable

@Serializable
data class OrderRequest(
    val side: Side,
    val quantity: BigDecimalJson,
    val price: BigDecimalJson,
    val pair: String,
)
