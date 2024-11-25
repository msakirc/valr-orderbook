package com.sakircimen.orderbook.utils

import java.math.BigDecimal
import java.math.MathContext

/**
 * returns inverse of multiplication of the given BigDecimal.
 * f(x) = 1/x
 */
fun BigDecimal.reverse(): BigDecimal {
    return this.pow(-1, MathContext.DECIMAL128)
}
