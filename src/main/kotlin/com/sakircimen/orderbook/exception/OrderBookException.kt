package com.sakircimen.orderbook.exception

import kotlinx.serialization.Serializable

@Serializable
open class OrderBookException(val code: Int, override val message: String?) : RuntimeException(message)

@Serializable
class CurrencyNotFoundException(private val currencyStr: String) : OrderBookException(
    code = -24,
    message = "Currency $currencyStr not found"
)

@Serializable
class InvalidCurrencyFormatException : OrderBookException(
    code = -101,
    message = "Invalid currency format."
)

@Serializable
class InvalidOrderFormatException : OrderBookException(
    code = -119,
    message = "Invalid order request. Please refer the docs for expected order information."
)

@Serializable
class InvalidPaginationException(private val maxPage: Int) : OrderBookException(
    code = -953,
    message = "No such page. Latest page is $maxPage"
)
