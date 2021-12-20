package com.hungngo.groceryappkotlin.model

import java.io.Serializable

class Order(
    val orderID: Long? = null,
    val orderBy: String? = null,
    val orderCost: Double? = null,
    val orderAddress: String? = null,
    val orderStatus: String? = null,
    val orderStartDate: Long? = null,
    var orderPaymentDate:Long? = null,
    var orderTo: String? = null,
    var items: MutableList<CartItem>? = null
): Serializable