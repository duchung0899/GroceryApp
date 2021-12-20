package com.hungngo.groceryappkotlin.model

class Message(
    val id: String,
    val text: String,
    val timestamp: Long,
    val fromID: String,
    val toID: String
){
    constructor(): this("","",-1,"","")
}