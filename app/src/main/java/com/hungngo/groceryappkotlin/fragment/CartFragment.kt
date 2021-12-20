package com.hungngo.groceryappkotlin.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.`interface`.IOnClickCartAdapter
import com.hungngo.groceryappkotlin.activity.MainBuyerActivity
import com.hungngo.groceryappkotlin.adapter.CartItemAdapter
import com.hungngo.groceryappkotlin.firebase.CartDAO
import com.hungngo.groceryappkotlin.model.CartItem
import com.hungngo.groceryappkotlin.model.Order
import com.hungngo.groceryappkotlin.model.User
import com.hungngo.groceryappkotlin.model.remoteFCM.FCMSendData
import com.hungngo.groceryappkotlin.remote.IFCMService
import com.hungngo.groceryappkotlin.remote.RetrofitFCMClient
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main_buyer.*
import kotlinx.android.synthetic.main.activity_main_buyer.view.*
import kotlinx.android.synthetic.main.fragment_cart.*
import kotlinx.android.synthetic.main.fragment_cart.view.*
import java.util.*

@SuppressLint("SetTextI18n")
class CartFragment : Fragment(), IOnClickCartAdapter, View.OnClickListener {

    private var mContext: Context? = null

    private lateinit var cartItemAdapter: CartItemAdapter
    private val listCartItem = mutableListOf<CartItem>()

    private val listShop = mutableListOf<User>()
    private val listStringShop = mutableListOf<String>()

    private lateinit var mAuth: FirebaseAuth
//    private lateinit var cartRef: DatabaseReference
//    private lateinit var userRef: DatabaseReference

    private var user: User? = null
    var totalDeliveryFee: Double = -1.0

    private val mCompositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        mAuth = FirebaseAuth.getInstance()

        val mainActivity = activity as MainBuyerActivity
        mainActivity.rl_buyer.visibility = View.VISIBLE
        mainActivity.rl_buyer.tv_buyer_title.text = "My Cart"

        view.btn_cart_buy.setOnClickListener(this)

//        cartRef = FirebaseDatabase.getInstance().getReference("Cart")
//        userRef = FirebaseDatabase.getInstance().getReference("Users")
//        mAuth = FirebaseAuth.getInstance()

        // get current user info
        user = arguments?.getSerializable("USER") as User?
        view.tv_cart_address.text = user?.address


        view.rcv_cart_item.layoutManager = LinearLayoutManager(requireContext())
        cartItemAdapter = CartItemAdapter(view.context, listCartItem, this)
        view.rcv_cart_item.adapter = cartItemAdapter

        loadCartFromDB()

        return view
    }

    private fun loadCartFromDB() {
        val ref = FirebaseDatabase.getInstance().getReference("Cart")
        ref.child(user!!.uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listCartItem.clear()
                listStringShop.clear()
                for (ds in snapshot.children) {
                    val strShopName = ds.key
                    listStringShop.add(strShopName!!)
                    getCartItem(strShopName)
                }
                calTotalDeliveryFee(listStringShop)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun getCartItem(strShopName: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("Cart").child(user!!.uid!!)
        ref.child(strShopName!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val cartItem = ds.getValue(CartItem::class.java)
                    listCartItem.add(cartItem!!)
                }
                cartItemAdapter.setCart(listCartItem)
                calTotalPrice(listCartItem)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    private fun calTotalDeliveryFee(listStringShop: MutableList<String>) {
        listShop.clear()
        totalDeliveryFee = 0.0
        val userRef = FirebaseDatabase.getInstance().reference.child("Users")
        listStringShop.forEach {
            userRef.child(it).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val shop = snapshot.getValue(User::class.java)
                    listShop.add(shop!!)
                    totalDeliveryFee += shop.deliveryFee!!

                    if (tv_cart_delivery_fee != null) {
                        tv_cart_delivery_fee.text = "$$totalDeliveryFee"
                    }

                    if (totalPrice != 0.0 && tv_cart_total != null) {
                        val total = totalDeliveryFee + totalPrice
                        tv_cart_total.text = "$$total"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    var totalPrice = 0.0
    private fun calTotalPrice(listCartItem: MutableList<CartItem>) {
        totalPrice = 0.0
        listCartItem.forEach {
            totalPrice += it.itemTotalPrice!!
        }
    }

    override fun increaseItem(cartItem: CartItem, total: Double) {
        tv_cart_total.text = "$$total"
        val cartRef = FirebaseDatabase.getInstance().reference.child("Cart")
        cartRef.child(user!!.uid!!).child(cartItem.itemSID!!).child(cartItem.itemPid!!.toString())
            .setValue(cartItem)

        totalPrice = total
    }

    override fun decreaseItem(cartItem: CartItem, total: Double) {
        tv_cart_total.text = "$$total"
        val cartRef = FirebaseDatabase.getInstance().reference.child("Cart")
        cartRef.child(user!!.uid!!).child(cartItem.itemSID!!).child(cartItem.itemPid!!.toString())
            .setValue(cartItem)

        totalPrice = total
    }

    override fun deleteItem(cartItem: CartItem, position: Int) {
//        val cartRef = FirebaseDatabase.getInstance().reference.child("Cart")
//        cartRef.child(user!!.uid!!).child(cartItem.itemSID!!).child(cartItem.itemPid!!.toString()).removeValue()
        CartDAO.deleteItemFromCart(cartItem, user!!.uid!!)
        listCartItem.removeAt(position)
        cartItemAdapter.notifyItemRemoved(position)
    }

    private fun placeOrder() {
        listShop.forEach { it ->
            val orderTo = it.uid
            val listItems = mutableListOf<CartItem>()
            val time = System.currentTimeMillis()
            val orderAddress = user!!.address
            val orderStatus = MyCommon.CONFIRM
            val orderBy = user!!.uid
            val orderPaymentDate: Long? = null
            val cartRef = FirebaseDatabase.getInstance().reference.child("Cart")
            cartRef.child(user!!.uid!!).child(it.uid!!).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listItems.clear()
                    for (ds in snapshot.children) {
                        val cartItem = ds.getValue(CartItem::class.java)
                        listItems.add(cartItem!!)
                    }
                    var orderCost = it.deliveryFee
                    if (orderCost != null) {
                        listItems.forEach {
                            orderCost += it.itemTotalPrice!!
                        }
                    }
                    val order = Order(
                        time, orderBy, orderCost, orderAddress, orderStatus,
                        time, orderPaymentDate, orderTo, listItems
                    )

                    val ref = FirebaseDatabase.getInstance().getReference("Order")
                    ref.child(orderBy!!).child(time.toString()).setValue(order)

//                    prepareNotificationMessage(time.toString(), it.uid!!)
                    sendNotification(order.orderID.toString(), it.uid!!)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
        Toast.makeText(mContext, "Place Order successfully", Toast.LENGTH_SHORT).show()
//        deleteItemsFromCart()
    }

    private fun sendNotification(orderID: String, shopID: String) {
        // when user places order. send notification to shops
        // prepare data for notification
        val NOTIFICATION_TOPIC = "/topics/${MyCommon.FCM_TOPIC_ORDER}"
        val NOTIFICATION_TITLE = "New Order $orderID"
        val NOTIFICATION_MESSAGE = "You have new order"
        val NOTIFICATION_TYPE = "New Order"

        val mapNotification = HashMap<String, String>()
        mapNotification["order_title"] = NOTIFICATION_TITLE
        mapNotification["order_content"] = NOTIFICATION_MESSAGE
        mapNotification["order_id"] = orderID
        mapNotification["order_sender"] = mAuth.uid!!
        mapNotification["order_receiver"] = shopID
        mapNotification["notificationType"] = NOTIFICATION_TYPE

        val fcmSendData = FCMSendData(NOTIFICATION_TOPIC, mapNotification)

        mCompositeDisposable.add(
            RetrofitFCMClient().getInstance().create(IFCMService::class.java)
                .sendNotification(fcmSendData)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                }, {
                    Toast.makeText(mContext, it.message, Toast.LENGTH_SHORT).show()
                })
        )
    }

    private fun deleteItemsFromCart() {
        val cartRef = FirebaseDatabase.getInstance().getReference("Cart")
        cartRef.child(user!!.uid!!).removeValue()
    }

//    private fun prepareNotificationMessage(orderId: String, shopId: String) {
//        // when user places order. send notification to shops
//
//        // prepare data for notification
//        val NOTIFICATION_TOPIC = "/topics/${MyCommon.FCM_TOPIC}"
//        val NOTIFICATION_TITLE = "New Order $orderId"
//        val NOTIFICATION_MESSAGE = "You have new order"
//        val NOTIFICATION_TYPE = "New Order"
//
//        // prepare json (what to send and where to send)
//        val notificationJs: JSONObject = JSONObject()
//        val notificationBodyJ0: JSONObject = JSONObject()
//        try {
//            // what to send
//            notificationBodyJ0.put("notificationType", NOTIFICATION_TYPE)
//            notificationBodyJ0.put("buyerUid", mAuth.uid) // buyer is current user
//            notificationBodyJ0.put("sellerUid", shopId)
//            notificationBodyJ0.put("orderId", orderId)
//            notificationBodyJ0.put("notificationTitle", NOTIFICATION_TITLE)
//            notificationBodyJ0.put("notificationMessage", NOTIFICATION_MESSAGE)
//            // where to send
//            notificationJs.put("to", NOTIFICATION_TOPIC) // to all who subscribed to this topic
//            notificationJs.put("data", notificationBodyJ0)
//        } catch (e: Exception) {
//            Toast.makeText(mContext, "${e.message}", Toast.LENGTH_SHORT).show()
//        }
//
//        sendOrderNotification(notificationJs, orderId)
//    }
//
//    private fun sendOrderNotification(notificationJo: JSONObject, orderId: String) {
//        // send volley request
//        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
//            "https://fcm.googleapis.com/fcm/send",
//            notificationJo,
//            Response.Listener {
//                // after sending fcm, start order details activity
//            },
//            Response.ErrorListener {
//                // if failed sending fcm, still start order details activity
//                Toast.makeText(mContext, "${it.message}", Toast.LENGTH_SHORT).show()
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
//        Volley.newRequestQueue(mContext).add(jsonObjectRequest)
//    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (mContext == null)
            mContext = context.applicationContext
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_cart_buy -> {
                placeOrder()
            }
        }
    }

}