package com.hungngo.groceryappkotlin.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hungngo.groceryappkotlin.MyApplication
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.activity.ChatLogActivity
import com.hungngo.groceryappkotlin.activity.OrderDetailSellerActivity
import com.hungngo.groceryappkotlin.activity.OrderDetailUserActivity
import com.hungngo.groceryappkotlin.model.Order
import com.hungngo.groceryappkotlin.model.User
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID" // required for android O and above

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    private var toUser: User? = null

//    private var order: Order? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        var intent: Intent? = null
        // get data from notification
        val notificationType = remoteMessage.data["notificationType"]
        if (notificationType == "New Order"){
            val receiverID = remoteMessage.data["order_receiver"]
            val senderID = remoteMessage.data["order_sender"]
            val orderId = remoteMessage.data["order_id"]
            val notificationTitle = remoteMessage.data["order_title"]
            val notificationDescription = remoteMessage.data["order_content"]

            intent = Intent(this, OrderDetailSellerActivity::class.java)
            intent.putExtra("orderID", orderId)
            intent.putExtra("orderBy", senderID)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            if (firebaseAuth.uid == receiverID)
                sendNotification(notificationTitle, notificationDescription, intent)
        }

        if (notificationType == "OrderStatusChanged"){
            val receiverID = remoteMessage.data["order_receiver"]
            val senderID = remoteMessage.data["order_sender"]
            val orderId = remoteMessage.data["order_id"]
            val notificationTitle = remoteMessage.data["order_title"]
            val notificationDescription = remoteMessage.data["order_content"]

            intent = Intent(this, OrderDetailUserActivity::class.java)
            intent.putExtra("orderID", orderId)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

            if (firebaseAuth.uid == receiverID)
                sendNotification(notificationTitle, notificationDescription, intent)
        }

        if (notificationType == "New Message"){
            val strMap = remoteMessage.data
            val strTitle = strMap["mess_title"]
            val strContent = strMap["mess_content"]
            val senderID = strMap["mess_sender"]
            val receiverID = strMap["mess_receiver"]
            val roomID = strMap["mess_roomID"]

//            getToUser(receiverID!!)

            intent = Intent(this, ChatLogActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            FirebaseDatabase.getInstance().getReference("Users").child(senderID!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        toUser = snapshot.getValue(User::class.java)
                        intent.putExtra("shop_object", toUser)

                        if (firebaseAuth.uid == receiverID && MyCommon.roomSelected != roomID)
                            sendNotification(strTitle, strContent, intent)
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    private fun sendNotification(strTitle: String?, strBody: String?, intent: Intent?) {
//        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        // large icon
//        val largeIcon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.shop)

        val notificationBuilder =  NotificationCompat.Builder(this, MyApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(strTitle)
            .setContentText(strBody)
            .setSmallIcon(R.drawable.shop)
//            .setLargeIcon(largeIcon)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        val notification = notificationBuilder.build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(Random.nextInt(), notification)
    }

    private fun getToUser(shopId: String) {
        FirebaseDatabase.getInstance().getReference("Users").child(shopId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    toUser = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(orderId: String, sellerUid : String, buyerUid: String, notificationTitle: String, notificationDescription: String, notificationType: String){
        // notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // id for notification, random
        val notificationId: Int = Random.nextInt(2000)

        // check if android version is Oreo or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setUpNotificationChannel(notificationManager)
        }

        // handle notification click, start activity
        var intent: Intent? = null
        if (notificationType == "NewOrder"){
            // buyer place order, open OrderDetailSellerActivity for seller
            intent = Intent(this, OrderDetailSellerActivity::class.java)
            intent.putExtra("order_object", getOrder(orderId, buyerUid))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        if (notificationType == "OrderStatusChanged"){
            // buyer place order, open OrderDetailSellerActivity for seller
            intent = Intent(this, OrderDetailUserActivity::class.java)
            if (order != null)
                intent.putExtra("order_object", getOrder(orderId, buyerUid))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        // large icon
        val largeIcon: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.shop)

        // sound of notification
        val notificationSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setLargeIcon(largeIcon)
            .setContentTitle(notificationTitle)
            .setContentText(notificationDescription)
            .setSound(notificationSoundUri)
            .setAutoCancel(true)    // cancel/ dismiss when clicked
            .setContentIntent(pendingIntent)

        // show notification
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "Some Sample Text"
        val channelDescription = "Channel Description here"

        val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.description = channelDescription
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)

        notificationManager.createNotificationChannel(notificationChannel)
    }

    var order: Order? = null
    private fun getOrder(orderId: String, buyerUid: String): Order? {
        FirebaseDatabase.getInstance().getReference("Order").child(buyerUid).child(orderId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    order = snapshot.getValue(Order::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        return order
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}