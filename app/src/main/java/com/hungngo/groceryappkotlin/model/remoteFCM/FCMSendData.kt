package com.hungngo.groceryappkotlin.model.remoteFCM

class FCMSendData(
    private val to: String = "",
    private val data: Map<String, String> = emptyMap()
)