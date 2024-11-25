package com.sakircimen.orderbook

import com.sakircimen.orderbook.controller.OrderBookController
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise

class Application : AbstractVerticle() {

  private val orderBookController = OrderBookController()

  override fun start(startPromise: Promise<Void>) {
    val port = 8082
    vertx
      .createHttpServer()
      .requestHandler(orderBookController.routes(vertx))
      .listen(port).onComplete { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port $port")
        } else {
          startPromise.fail(http.cause());
        }
      }
  }
}
