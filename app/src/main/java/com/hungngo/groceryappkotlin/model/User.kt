package com.hungngo.groceryappkotlin.model

import java.io.Serializable

class User(
    var name: String? = null,
    var shopName: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var deliveryFee: Double? = 0.0,
    var country: String? = null,
    var state: String? = null,
    var city: String? = null,
    var address: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var timestamp: Long? = null,
    var online: Boolean? = null,
    var shopOpen: Boolean? = null,
    var profileImage: String? = null,
    var userType: String? = null,
    var uid: String? = null
): Serializable