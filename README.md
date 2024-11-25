# Order Book

This repository serves a basic prototype for an order book for VALR interview

## Used Technologies

- [Kotlin](https://kotlinlang.org/)
- [Gradle](https://gradle.org/)
- [Vert.x](https://vertx.io/)

## Running the service

Run `gradlew run` to start the server. Default port is 8082

## Auth

I have used properties file based authentication for simplicity. See `resources/auth.properties` for valid username /
password combinations. I disabled sessions to keep testing simple.

## Known issues and missing points

* Auth is very simple and not very secure for the sake of simplicity.
* No database setup.
* No concurrency.
* Same-priced orders. Didn't have time to setup aggregation of these orders. Order count will always be 1
* Pagination for all requests. Only trade history endpoint has basic pagination. Having too many orders could cause
  problems.
* Not tested well enough. No tests for endpoints, exception cases etc.
* Only 3 currencies available but it supports operations for all of them in both directions
* Only 3 letter currencies is supported at the moment. I found some currency listing endpoint within VALR APIs but
  didn't have time to implement further.
* No user-related operations, no links of orders with users
* No order tracking via id etc.
* Could be optimized more: e.g. more effective search algorithms instead of traversing each order etc.

## Maintainer

Şakir Çimen [(sakircimen@gmail.com)](mailto:sakircimen@gmail.com)
