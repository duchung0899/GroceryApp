package com.hungngo.groceryappkotlin.model

import java.io.Serializable

class Product (
    val productId: Long?= 0,
    val productTitle: String?= null,
    val productDescription: String?= null,
    val productCategory: String?= null,
    val productQuantity: Int?= null,
    var productIcon: String?= null,
    val productPrice: String?= null,
    val discountPrice: String?= null,
    val discountNote: String?= null,
    val discountAvailable: Boolean?= null,
    val timestamp: Long?= null,
    val uid: String?= null,
): Serializable