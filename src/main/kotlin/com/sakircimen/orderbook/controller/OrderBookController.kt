package com.sakircimen.orderbook.controller

import com.sakircimen.orderbook.exception.InvalidOrderFormatException
import com.sakircimen.orderbook.exception.OrderBookException
import com.sakircimen.orderbook.service.OrderService
import com.sakircimen.orderbook.service.TradeService
import com.sakircimen.orderbook.web.OrderRequest
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class OrderBookController {
    private val tradeService = TradeService()
    private val orderService = OrderService(tradeService)

    fun routes(vertx: Vertx): Router {
        val router = Router.router(vertx)
        router.get("/:currencyStr/orderbook")
            .produces("application/json")
            .handler { handleOrderBookListRequest(it) }
            .failureHandler { handleException(it) }

        router.post("/v1/orders/limit")
            .consumes("application/json")
            .handler(BodyHandler.create())
            .handler { handlePlaceOrderRequest(it) }
            .failureHandler { handleException(it) }

        router.get("/:currencyStr/tradehistory")
            .produces("application/json")
            .handler { handleGetTradeHistoryRequest(it) }
            .failureHandler { handleException(it) }

        return router
    }

    private fun handleOrderBookListRequest(context: RoutingContext): Future<Void>? {
        val result = orderService.listOrders(context.pathParam("currencyStr"))
        return context.response().setStatusCode(200).end(
            Json.encodeToString(result)
        )
    }
    private fun handleGetTradeHistoryRequest(context: RoutingContext): Future<Void>? {
        val result = tradeService.getTradeHistory(context.pathParam("currencyStr"))
        return context.response().setStatusCode(200).end(
            Json.encodeToString(result)
        )
    }

    private fun handlePlaceOrderRequest(context: RoutingContext): Future<Void>? {
        val orderRequest = try {
            Json.decodeFromString<OrderRequest>(context.body().asString())
        } catch (e: SerializationException) {
            return context.response().setStatusCode(400).end(
                Json.encodeToString(InvalidOrderFormatException())
            )
        }
        val message = orderService.placeOrder(orderRequest)
        return context.response().setStatusCode(200).end(
            Json.encodeToString(
                mapOf(
                    "status" to "ok",
                    "message" to message
                )
            )
        )
    }

    private fun handleException(it: RoutingContext) {
        val error = it.failure()
        if (error is OrderBookException) {
            it.response().setStatusCode(404).end(Json.encodeToString(error))
        }
    }
}