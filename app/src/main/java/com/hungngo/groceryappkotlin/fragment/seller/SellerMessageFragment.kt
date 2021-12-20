package com.hungngo.groceryappkotlin.fragment.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.adapter.item.LatestMessageItem
import com.hungngo.groceryappkotlin.model.Message
import com.xwray.groupie.GroupieAdapter
import kotlinx.android.synthetic.main.fragment_seller_message.view.*


class SellerMessageFragment : Fragment() {
    val adapter = GroupieAdapter()

    val latestMessageMap = HashMap<String, Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.rcv_message_in_shop.adapter = adapter
        view.rcv_message_in_shop.layoutManager = LinearLayoutManager(view.context)

        // add divider decoration
        view.rcv_message_in_shop.addItemDecoration(DividerItemDecoration(
            view.context,
            LinearLayoutManager.VERTICAL
        ))

        listenForLatestMessage()
    }

    private fun listenForLatestMessage() {
        val fromID = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("LatestMessage/$fromID")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(Message::class.java) ?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(Message::class.java) ?: return
                latestMessageMap[snapshot.key!!] = chatMessage
                refreshRecyclerView()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun refreshRecyclerView() {
        adapter.clear()
        latestMessageMap.values.forEach{
            adapter.add(LatestMessageItem(it))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SellerMessageFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}

