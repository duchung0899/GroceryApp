package com.hungngo.groceryappkotlin.adapter.item

import android.content.Intent
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.activity.ChatLogActivity
import com.hungngo.groceryappkotlin.model.Message
import com.hungngo.groceryappkotlin.model.User
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.row_latest_message_user.view.*
import java.text.SimpleDateFormat
import java.util.*

class LatestMessageItem(val message: Message): Item<GroupieViewHolder>(){

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        // set message
        if (message.fromID == FirebaseAuth.getInstance().uid){
            viewHolder.itemView.tv_latest_message_text.text = String.format("You: %s", message.text)
        }else{
            viewHolder.itemView.tv_latest_message_text.text = message.text
        }

        // set date
        val simpleDateFormat = SimpleDateFormat("dd/MM/yy hh:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = message.timestamp
        viewHolder.itemView.tv_latest_message_time.text = simpleDateFormat.format(calendar.time)

        // get user info
        val chatPartnerID: String = if (message.fromID == FirebaseAuth.getInstance().uid){
            message.toID
        }else{
            message.fromID
        }
        val ref = FirebaseDatabase.getInstance().getReference("Users/$chatPartnerID")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                if (user?.userType == MyCommon.BUYER)
                    viewHolder.itemView.tv_latest_message_name.text = user.name
                else
                    viewHolder.itemView.tv_latest_message_name.text = user?.shopName

                val desImage = viewHolder.itemView.img_latest_message
                Glide.with(viewHolder.itemView.context).load(user?.profileImage)
                    .placeholder(R.drawable.ic_person_gray).into(desImage)

                if (user?.online == true) {
                    viewHolder.itemView.img_latest_message_status.visibility = View.VISIBLE
                } else {
                    viewHolder.itemView.img_latest_message_status.visibility = View.GONE
                }

                viewHolder.itemView.setOnClickListener { view ->
                    Intent(view.context, ChatLogActivity::class.java).apply {
                        this.putExtra("shop_object", user)
                        val roomId = MyCommon.generateChatRoomId(message.fromID, message.toID)
                        MyCommon.roomSelected = roomId
                        view.context.startActivity(this)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getLayout(): Int {
        return R.layout.row_latest_message_user
    }
}
