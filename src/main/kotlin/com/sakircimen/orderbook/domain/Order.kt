package com.sakircimen.orderbook.domain

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Order(
    val id: UUID = UUID.randomUUID(),
    val createdAt: LocalDateTime = LocalDateTime.now(),

    val side: Side,
    val quantity: BigDecimal,
    val originalPrice: BigDecimal,
    val effectivePrice: BigDecimal,
    val effectiveCurrencyPair: Pair<Currency, Currency>,

    // only one of below fields are necessary, putting both fields just for improving performance
    val originalCurrencyPair: Pair<Currency, Currency>,
    val currencyPairReversed: Boolean,
)
