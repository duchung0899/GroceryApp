package com.hungngo.groceryappkotlin.adapter.item

import com.bumptech.glide.Glide
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.model.User
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.row_chat_to.view.*

class ChatToItem(val message: String, val user: User): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.row_chat_to
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_to.text = message

        val imgToUser = viewHolder.itemView.img_chat_to

        Glide.with(viewHolder.itemView.context).load(user.profileImage)
            .placeholder(R.drawable.ic_person_gray).into(imgToUser)
    }
}