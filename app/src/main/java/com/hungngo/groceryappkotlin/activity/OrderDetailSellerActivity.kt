package com.hungngo.groceryappkotlin.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.adapter.OrderItemAdapter
import com.hungngo.groceryappkotlin.model.CartItem
import com.hungngo.groceryappkotlin.model.Order
import com.hungngo.groceryappkotlin.model.remoteFCM.FCMSendData
import com.hungngo.groceryappkotlin.remote.IFCMService
import com.hungngo.groceryappkotlin.remote.RetrofitFCMClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_order_detail.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

@SuppressLint("SetTextI18n")
class OrderDetailSellerActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var orderItemAdapter: OrderItemAdapter
    private val listOrderItems = mutableListOf<CartItem>()

    private var order: Order? = null

    private val mCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)

        // set onClick
        img_chat_log_back.setOnClickListener(this)
        img_edit_order_status.setOnClickListener(this)

        // get chose order
        getOrder()
    }

    private fun getOrder() {
        order = intent.getSerializableExtra("order_object") as Order?
        if (order!= null){
            setUpInformation()
            Log.d("TAG", "getOrder: not null")
        }
        else{
            val orderID = intent.getStringExtra("orderID")
            val orderBy = intent.getStringExtra("orderBy")
            FirebaseDatabase.getInstance().getReference("Order")
                .child(orderBy!!).child(orderID!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                            order = snapshot.getValue(Order::class.java)
                            setUpInformation()
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    private fun setUpInformation() {
        Log.d("TAG", "setUpInformation: ${order?.orderCost}")
        tv_id_order.text = order!!.orderID.toString()
        tv_status_order.text = order!!.orderStatus
        tv_total_order.text = "$${order!!.orderCost}"
        tv_address_order.text = order!!.orderAddress
        tv_number_item.text = "All Item [${order!!.items!!.size}]"
        // set start date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = order!!.orderStartDate!!
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault())
        tv_start_date.text = simpleDateFormat.format(calendar.time)

        // set user info (name, phone)
        setClientInfo(order!!.orderBy)

        // set list items to listOrderItems
        listOrderItems.addAll(order!!.items!!)
        // init adapter
        orderItemAdapter = OrderItemAdapter(this, listOrderItems)
        // init recyclerView
        rcv_order_item.adapter = orderItemAdapter
        rcv_order_item.layoutManager = LinearLayoutManager(this)
    }

    private fun setClientInfo(orderBy: String?) {
        FirebaseDatabase.getInstance().getReference("Users").child(orderBy!!)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userName = snapshot.child("name").value as String
                    val phoneNumber = snapshot.child("phone").value as String

                    tv_name_order.text = userName
                    tv_phone_order.text = phoneNumber
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun showDialogStatusOrder() {
        val options = arrayOf(MyCommon.CONFIRM, MyCommon.PROGRESS, MyCommon.COMPLETED, MyCommon.CANCEL)
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Change status of order")
        dialog.setItems(options) { _, which ->
            editOrderStatus(options[which])
        }
        dialog.show()
    }

    private fun editOrderStatus(strSelection: String) {
        val map = mutableMapOf<String, Any>("orderStatus" to strSelection)
        // if order is shipped, set orderPaymentDate to DB
        if (strSelection == MyCommon.COMPLETED){
            val endDate = System.currentTimeMillis()
            map["orderPaymentDate"] = endDate
        }
        // update Order
        FirebaseDatabase.getInstance().getReference("Order").child(order!!.orderBy!!)
            .child(order!!.orderID.toString()).updateChildren(map)
            .addOnSuccessListener {
                val message = "Order is now $strSelection"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//                prepareNotificationMessage(order!!.orderID.toString(), message)
                sendNotification(order!!.orderID.toString(), message)
            }
            .addOnFailureListener {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }

        // update orderStatus TextView
        tv_status_order.text = strSelection
    }

    private fun sendNotification(orderId: String, message: String) {
        // when seller change status of order, send notification to user

        // prepare data for notification
        val NOTIFICATION_TOPIC = "/topics/${MyCommon.FCM_TOPIC_ORDER}" // must be same as subscribed by user
        val NOTIFICATION_TITLE = "Your Order $orderId"
        val NOTIFICATION_MESSAGE = message
        val NOTIFICATION_TYPE = "OrderStatusChanged"

        val mapNotification = HashMap<String, String>()
        mapNotification["order_title"] = NOTIFICATION_TITLE
        mapNotification["order_content"] = NOTIFICATION_MESSAGE
        mapNotification["order_id"] = orderId
        mapNotification["order_sender"] = order!!.orderTo!!
        mapNotification["order_receiver"] = order!!.orderBy!!
        mapNotification["notificationType"] = NOTIFICATION_TYPE

        val fcmSendData = FCMSendData(NOTIFICATION_TOPIC, mapNotification)

        mCompositeDisposable.add(
            RetrofitFCMClient().getInstance().create(IFCMService::class.java)
                .sendNotification(fcmSendData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                })
        )

    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
    }

//    private fun prepareNotificationMessage(orderId: String, message: String) {
//        // when seller change status of order, send notification to user
//
//        // prepare data for notification
//        val NOTIFICATION_TOPIC = "/topics/${MyCommon.FCM_TOPIC}" // must be same as subscribed by user
//        val NOTIFICATION_TITLE = "Your Order $orderId"
//        val NOTIFICATION_MESSAGE = message
//        val NOTIFICATION_TYPE = "OrderStatusChanged"
//
//        // prepare json (what to send and where to send)
//        val notificationJo: JSONObject = JSONObject()
//        val notificationBodyJo: JSONObject = JSONObject()
//        try {
//            // what to send
//            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE)
//            notificationBodyJo.put("buyerUid", order?.orderBy)
//            notificationBodyJo.put("sellerUid", order?.orderBy)
//            notificationBodyJo.put("orderId", orderId)
//            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE)
//            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE)
//            // where to send
//            notificationJo.put("to", NOTIFICATION_TOPIC) // to all who subscribed to this topic
//            notificationJo.put("data", notificationBodyJo)
//        } catch (e: Exception) {
//            Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
//        }
//
//        sendOrderNotification(notificationJo)
//    }
//
//    private fun sendOrderNotification(notificationJo: JSONObject) {
//        // send volley request
//        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
//            "https://fcm.googleapis.com/fcm/send",
//            notificationJo,
//            Response.Listener {
//                // after sending fcm, start order details activity
//            },
//            Response.ErrorListener {
//                // if failed sending fcm, still start order details activity
//                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
//            }) {
//            @Throws(AuthFailureError::class)
//            override fun getHeaders(): Map<String, String> {
//                val headers: MutableMap<String, String> = HashMap()
//                headers["Content-Type"] = "application/json"
//                headers["Authorization"] = "key=${MyCommon.FCM_KEY}"
//                return headers
//            }
//        }
//        // enqueue the volley request
//        Volley.newRequestQueue(this).add(jsonObjectRequest)
//    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.img_chat_log_back -> {
//                startActivity(Intent(this, MainSellerActivity::class.java))
                onBackPressed()
            }
            R.id.img_edit_order_status -> {
                showDialogStatusOrder()
            }
        }
    }
}