package com.hungngo.groceryappkotlin.fragment.tab_in_order

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.adapter.ReviewAdapter
import com.hungngo.groceryappkotlin.adapter.testNestedRecyclerView.ListOrderAdapter
import com.hungngo.groceryappkotlin.model.CartItem
import com.hungngo.groceryappkotlin.model.Order
import com.hungngo.groceryappkotlin.model.Review
import kotlinx.android.synthetic.main.activity_user_order.*
import kotlinx.android.synthetic.main.dialog_review.*
import kotlinx.android.synthetic.main.fragment_confirm.*

class ConfirmFragment : Fragment(), ListOrderAdapter.IOnClickListener {
    private var mContext: Context? = null

    private var orderState: String? = null

    private lateinit var listOrderAdapter: ListOrderAdapter
    private lateinit var mAuth: FirebaseAuth

    private val listOrder = mutableListOf<Order>()

    private lateinit var rcvOrderTab : RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // mapping
        rcvOrderTab = view.findViewById(R.id.rcv_order_tab)

        orderState = arguments?.getString("Order_State")

        mAuth = FirebaseAuth.getInstance()

        listOrderAdapter = ListOrderAdapter(mContext!!, listOrder, this)

        getShopInfo()
    }

    private fun getShopInfo() {
        val orderRef = FirebaseDatabase.getInstance().getReference("Order")
        orderRef.child(mAuth.uid!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOrder.clear()
                for (ds in snapshot.children) {
                    val order = ds.getValue(Order::class.java)
                    if (order!!.orderStatus == orderState)
                        listOrder.add(order)
                }
                listOrder.sortByDescending { it.orderStartDate }
                listOrderAdapter.setListOrder(listOrder)
                rcvOrderTab.adapter = listOrderAdapter
                rcvOrderTab.layoutManager = LinearLayoutManager(mContext)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (mContext == null)
            mContext = context.applicationContext
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    override fun onClickButtonReBuy() {
        Log.d("TAG", "onClickButtonReBuy: 123")
    }

}