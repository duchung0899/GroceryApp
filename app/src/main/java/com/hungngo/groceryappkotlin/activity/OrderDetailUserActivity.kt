package com.hungngo.groceryappkotlin.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.adapter.OrderItemInDetailAdapter
import com.hungngo.groceryappkotlin.model.CartItem
import com.hungngo.groceryappkotlin.model.Order
import com.hungngo.groceryappkotlin.model.Review
import kotlinx.android.synthetic.main.activity_order_detail_user.*
import kotlinx.android.synthetic.main.dialog_review.*
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetTextI18n")
class OrderDetailUserActivity : AppCompatActivity(), View.OnClickListener, OrderItemInDetailAdapter.IOnClickToItem {
    private lateinit var orderItemAdapter: OrderItemInDetailAdapter
    private val listOrderItems = mutableListOf<CartItem>()

    private var order: Order? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail_user)

        img_chat_log_back.setOnClickListener(this)
        btn_cancel_order.setOnClickListener(this)

        getOrder()
    }

    private fun getOrder() {
        order = intent.getSerializableExtra("order_object") as Order?
        if (order!= null){
            setUpInformation()
        }else{
            val orderID = intent.getStringExtra("orderID")
            if (orderID != null){
                Log.d("TAG", "getOrder: $orderID")
                FirebaseDatabase.getInstance().getReference("Order")
                    .child(FirebaseAuth.getInstance().uid!!).child(orderID)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                order = snapshot.getValue(Order::class.java)
                                setUpInformation()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
            }
        }
    }

    private fun setUpInformation() {
        tv_id_order.text = order!!.orderID.toString()
        tv_status_order.text = order!!.orderStatus
        tv_total_order.text = "$${order!!.orderCost}"
        tv_address_order.text = order!!.orderAddress
        tv_number_item.text = "All Items [${order!!.items!!.size}]"
        // set start date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = order!!.orderStartDate!!
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.getDefault())
        tv_start_date.text = simpleDateFormat.format(calendar.time)

        // set user info (name, phone)
        setShopInfo(order!!.orderTo)

        // set list items to listOrderItems
        listOrderItems.addAll(order!!.items!!)
        // init adapter
        orderItemAdapter = OrderItemInDetailAdapter(this@OrderDetailUserActivity, listOrderItems, this)
        // init recyclerView
        rcv_order_item.adapter = orderItemAdapter
        rcv_order_item.layoutManager = LinearLayoutManager(this)

        // if orderStatus is not equal confirm, client can't cancel order
        when(order!!.orderStatus) {
            MyCommon.CONFIRM -> {
                btn_cancel_order.visibility = View.VISIBLE
                tv_swipe_instruction.visibility = View.GONE
            }
            MyCommon.COMPLETED -> {
                tv_swipe_instruction.visibility = View.VISIBLE
                btn_cancel_order.visibility = View.GONE
            }
            else -> {
                btn_cancel_order.visibility = View.GONE
                tv_swipe_instruction.visibility = View.GONE
            }
        }
    }

    private fun setShopInfo(orderTo: String?) {
        FirebaseDatabase.getInstance().getReference("Users").child(orderTo!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val shopName = snapshot.child("shopName").value as String
                    val phoneNumber = snapshot.child("phone").value as String

                    tv_name_order.text = shopName
                    tv_phone_order.text = phoneNumber
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }


    private fun showDialogCancel() {
        AlertDialog.Builder(this).apply {
            setTitle("Cancel Order")
            setMessage("Are you sure to cancel this order")
            setIcon(R.drawable.ic_person_gray)
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            setPositiveButton("Yes") { _, _ ->
                val map = mutableMapOf<String, Any>()
                map["orderStatus"] = MyCommon.CANCEL
                FirebaseDatabase.getInstance().getReference("Order").child(order?.orderBy!!)
                    .child(order?.orderID.toString()).updateChildren(map)
                Toast.makeText(this@OrderDetailUserActivity, "Your order now is cancel", Toast.LENGTH_SHORT).show()
                finish()
            }
        }.create().show()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.img_chat_log_back -> {
                onBackPressed()
            }
            R.id.btn_cancel_order -> {
                showDialogCancel()
            }
        }
    }

    override fun clickToReview(itemPid: Long?) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_review)
        dialog.setCanceledOnTouchOutside(false) // not cancel when user click outside dialog
        // set layout width is match parent
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // get current review
        getReview(dialog, itemPid)

        // on click button
        dialog.btn_cancel_review.setOnClickListener {
            dialog.dismiss()
        }
        dialog.btn_submit_review.setOnClickListener{
            val time = System.currentTimeMillis()
            val numStar = dialog.rb_rating.rating
            val experience = dialog.edt_review.text.toString()
            val review = Review(order!!.orderBy!!, numStar, experience , time)
            addReviewToDB(review, itemPid)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun getReview(dialog: Dialog, itemPid: Long?) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        userRef.child(order!!.orderTo!!).child("Products").child(itemPid.toString())
            .child("Review").child(order!!.orderBy!!).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val review : Review? = snapshot.getValue(Review::class.java)
                        if (review != null){
                            dialog.rb_rating.rating = review.numStar!!
                            dialog.edt_review.setText(review.experience)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun addReviewToDB(review: Review, itemPid: Long?) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        userRef.child(order!!.orderTo!!).child("Products").child(itemPid.toString())
            .child("Review").child(review.idReview.toString()).setValue(review)
            .addOnSuccessListener {
                Toast.makeText(this, "Write review successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}