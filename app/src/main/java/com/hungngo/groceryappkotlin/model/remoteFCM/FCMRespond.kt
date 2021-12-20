package com.hungngo.groceryappkotlin.model.remoteFCM

class FCMRespond(
    private val multicast_id: Long,
    private val success: Int,
    private val failure: Int,
    private val canonical_ids: Int,
    private val results: List<FCMResult>,
    private val message_id: Long
) {
    constructor(): this(-1, -1, -1, -1, emptyList(), -1)
}