package com.hungngo.groceryappkotlin

import java.lang.StringBuilder
import kotlin.random.Random

object MyCommon {
    // permission constants
    const val LOCATION_REQUEST_CODE = 100
    const val CAMERA_REQUEST_CODE = 200
    const val STORAGE_REQUEST_CODE = 300

    // image pick constants
    const val IMAGE_PICK_GALLERY_CODE = 400
    const val IMAGE_PICK_CAMERA_CODE = 500

    // user type constants
    const val SELLER = "seller"
    const val BUYER = "buyer"

    // product category
    val productCategory = arrayOf(
        "Beverages", "Beauty & Personal Care", "Snacks", "Chocolate",
        "Dairy", "Frozen Food", "Fruits",
        "Pet Care", "Pharmacy", "Vegetables", "Others"
    )

    val productCategory1= arrayOf(
        "All", "Beverages", "Beauty & Personal Care", "Snacks", "Chocolate",
         "Dairy", "Frozen Food", "Fruits",
        "Pet Care", "Pharmacy", "Vegetables", "Others"
    )

    // order status
    const val PROGRESS = "In Progress"
    const val COMPLETED = "Completed"
    const val CONFIRM = "Confirm"
    const val CANCEL = "Cancel"

    val orderStateList1 = arrayOf(CONFIRM, PROGRESS, COMPLETED, CANCEL)

    // list state of order
    val orderStatesList = mutableListOf("Confirm", "Pending", "Completed", "Cancel", "Review")

    // firebase cloud message
    const val FCM_KEY = ""
    const val FCM_TOPIC_ORDER = "PUSH_NOTIFICATIONS"
    const val FCM_TOPIC_MESSAGE = "MESSAGE"


    // generate chat room id to know user is in room or not
    fun generateChatRoomId(a: String, b : String): String{
        return when {
            a > b -> StringBuilder(a).append(b).toString()
            a < b -> StringBuilder(b).append(a).toString()
            else -> StringBuilder("Chat_Your_self_Error").append(Random(3000).nextInt()).toString()
        }
    }
    var roomSelected = "" // store roomId of user, if user is not in chatLog Activity, it set to be ""

    // shared preferences key
    const val SP_KEY = "settings_sp"
    const val SP_ORDER_NOTI_KEY = "order_notification"
    const val SP_MESSAGE_NOTI_KEY = "message_notification"
}
