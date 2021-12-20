package com.hungngo.groceryappkotlin.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.adapter.item.LatestMessageItem
import com.hungngo.groceryappkotlin.model.Message
import com.hungngo.groceryappkotlin.model.User
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.fragment_message.view.*
import kotlinx.android.synthetic.main.row_latest_message_user.view.*


class MessageFragment : Fragment() {
    val adapter = GroupieAdapter()

    val latestMessageMap = HashMap<String, Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.rcv_latest_message_user.adapter = adapter
        view.rcv_latest_message_user.layoutManager = LinearLayoutManager(view.context)

        // add divider decoration
        view.rcv_latest_message_user.addItemDecoration(DividerItemDecoration(
            view.context,
            LinearLayoutManager.VERTICAL
        ))

        listenForLatestMessage()
    }

    private fun listenForLatestMessage() {
        val fromID = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("LatestMessage/$fromID")
        ref.addChildEventListener(object : ChildEventListener{
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
}



