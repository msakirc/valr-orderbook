package com.sakircimen.orderbook.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.util.*

data class Trade(
    val price: BigDecimal,
    val quantity: BigDecimal,
    val currencyPair: Pair<Currency, Currency>,
    val tradedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val takerSide: Side,
    val sequenceId: Int,

    val id: UUID = UUID.randomUUID(),
    val quoteVolume: BigDecimal,
)
