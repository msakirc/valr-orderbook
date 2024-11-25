package com.sakircimen.orderbook.utils

import com.sakircimen.orderbook.domain.Currency
import com.sakircimen.orderbook.exception.CurrencyNotFoundException
import com.sakircimen.orderbook.exception.InvalidCurrencyFormatException

fun Pair<Currency, Currency>.reverse(): Pair<Currency, Currency> {
    return this.second to this.first
}

fun parseCurrencyPair(currencyStr: String): Pair<Currency, Currency> {
    if (currencyStr.length != 6)
        throw InvalidCurrencyFormatException()

    val firstCurrency = parseCurrency(currencyStr.substring(0, 3))
    val secondCurrency = parseCurrency(currencyStr.substring(3, 6))
    val currencyPair = firstCurrency to secondCurrency
    return currencyPair
}

fun parseCurrency(currencyStr: String): Currency {
    try {
        return Currency.valueOf(currencyStr)
    } catch (e: IllegalArgumentException) {
        throw CurrencyNotFoundException(currencyStr)
    }
}

fun currencyPairToString(pair: Pair<Currency, Currency>) =
    "${pair.first}${pair.second}"
