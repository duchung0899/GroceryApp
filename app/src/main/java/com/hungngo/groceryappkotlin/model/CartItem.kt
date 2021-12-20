package com.hungngo.groceryappkotlin.model

import java.io.Serializable


class CartItem (
    val itemSID: String? = null,
    val itemPid: Long? = null,
    val itemName: String? = null,
    val itemIcon: String? = null,
    val itemUnitPrice: Double? = null,
    var itemQuantity: Int? = null,
    var itemTotalPrice: Double? = null
): Serializable {
    override fun toString(): String {
        return "$itemPid, $itemName, $itemUnitPrice, $itemQuantity, $itemTotalPrice"
    }
}