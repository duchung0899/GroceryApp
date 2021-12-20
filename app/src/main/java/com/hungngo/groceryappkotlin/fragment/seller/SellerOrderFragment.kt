package com.hungngo.groceryappkotlin.fragment.seller

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.adapter.SellerOrderAdapter
import com.hungngo.groceryappkotlin.model.Order
import kotlinx.android.synthetic.main.fragment_seller_order.*
import kotlinx.android.synthetic.main.fragment_seller_order.view.*

class SellerOrderFragment : Fragment() {

    // adapter
    lateinit var orderAdapter: SellerOrderAdapter
    private var listOrder = mutableListOf<Order>()

    private val orderMap = HashMap<String, Order>()

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller_order, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        orderAdapter = SellerOrderAdapter(view.context, listOrder)
        view.rcv_seller_order.adapter = orderAdapter
        view.rcv_seller_order.layoutManager = LinearLayoutManager(view.context)

        view.rcv_seller_order.addItemDecoration(
            DividerItemDecoration(
                view.context,
                LinearLayoutManager.VERTICAL
            )
        )

        loadMyOrder1()

        view.img_seller_filter.setOnClickListener{
            showOrderStateDialog()
        }
    }

    private fun loadMyOrder() {
        // get userID first to point to userId in Order tree (Order -> userID -> orderItem)
        FirebaseDatabase.getInstance().getReference("Users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listOrder.clear()
                    for (ds in snapshot.children) {
                        val userID = ds.child("uid").value as String
                        // get userID successfully -> get it's orders in Order tree
                        FirebaseDatabase.getInstance().getReference("Order").child(userID)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        for (snap in snapshot.children) {
                                            val order = snap.getValue(Order::class.java)
                                            // if this order is ordered to this shop -> add to list
                                            if (order!!.orderTo == mAuth.uid) {
                                                listOrder.add(order)
                                            }
                                        }
                                        orderAdapter.setOrders(listOrder)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun loadMyOrder1() {
        // get userID first to point to userId in Order tree (Order -> userID -> orderItem)
        FirebaseDatabase.getInstance().getReference("Users")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listOrder.clear()
                    for (ds in snapshot.children) {
                        val userID = ds.child("uid").value as String
                        // get userID successfully -> get it's orders in Order tree
                        FirebaseDatabase.getInstance().getReference("Order").child(userID)
                            .addChildEventListener(object : ChildEventListener {
                                override fun onChildAdded(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {
                                    val order = snapshot.getValue(Order::class.java)
                                    // if this order is ordered to this shop -> add to list
                                    if (order!!.orderTo == mAuth.uid
                                        && order.orderStatus != MyCommon.CANCEL
                                        && order.orderStatus != MyCommon.COMPLETED
                                    ) {
                                        orderMap[snapshot.key!!] = order
                                        refreshRecyclerView()
                                    }
                                }

                                override fun onChildChanged(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {
                                    val order = snapshot.getValue(Order::class.java)
                                    // if this order is ordered to this shop -> add to list
                                    if (order!!.orderTo == mAuth.uid
                                        && order.orderStatus != MyCommon.CANCEL
                                        && order.orderStatus != MyCommon.COMPLETED
                                    ) {
                                        orderMap[snapshot.key!!] = order
                                        refreshRecyclerView()
                                    }
                                }

                                override fun onChildRemoved(snapshot: DataSnapshot) {

                                }

                                override fun onChildMoved(
                                    snapshot: DataSnapshot, previousChildName: String?
                                ) {

                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun refreshRecyclerView() {
        listOrder.clear()
        orderMap.values.forEach{
            listOrder.add(it)
        }
        listOrder.sortWith(compareBy<Order> { it.orderStatus }.thenBy { it.orderStartDate })
        orderAdapter.setOrders(listOrder)
    }

    private fun showOrderStateDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Order State")
            setItems(MyCommon.orderStateList1) { _, which ->
                val cateSelected = MyCommon.orderStateList1[which]
                tv_show_order_status.text = cateSelected
            }
            show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SellerOrderFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}