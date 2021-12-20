package com.hungngo.groceryappkotlin.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.adapter.item.ChatFromItem
import com.hungngo.groceryappkotlin.adapter.item.ChatToItem
import com.hungngo.groceryappkotlin.model.Message
import com.hungngo.groceryappkotlin.model.User
import com.hungngo.groceryappkotlin.model.remoteFCM.FCMSendData
import com.hungngo.groceryappkotlin.remote.IFCMService
import com.hungngo.groceryappkotlin.remote.RetrofitFCMClient
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.row_chat_from.view.*
import kotlinx.android.synthetic.main.row_chat_to.view.*

class ChatLogActivity : AppCompatActivity(), View.OnClickListener {

    private var toUser: User? = null
    private var fromUser: User? = null
    private val chatAdapter = GroupieAdapter()

    private lateinit var mAuth: FirebaseAuth

    private var ifcmService: IFCMService? = null
    private val mCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        ifcmService = RetrofitFCMClient().getInstance().create(IFCMService::class.java)

        mAuth = FirebaseAuth.getInstance()

        btn_send_message.setOnClickListener(this)
        img_chat_log_back.setOnClickListener(this)

        getShopInfo()
        getCurrentUser()

        listenForMessage()

        rcv_message.adapter = chatAdapter
    }

    private fun getShopInfo() {
        toUser = intent.getSerializableExtra("shop_object") as User?
        if (toUser != null) {
            setToUserInfo()
            MyCommon.roomSelected = MyCommon.generateChatRoomId(mAuth.uid!!, toUser?.uid!!)
        }
//        else {
//            val toUserID = intent.getStringExtra("shop_id")
//            FirebaseDatabase.getInstance().getReference("Users").child(toUserID!!)
//                .addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        toUser = snapshot.getValue(User::class.java)
//                        setToUserInfo()
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                    }
//                })
//        }
    }

    private fun setToUserInfo() {
        if (toUser!!.userType == MyCommon.SELLER) {
            tv_chat_user_name.text = toUser!!.shopName
        } else {
            tv_chat_user_name.text = toUser!!.name
        }

        if (toUser!!.profileImage != null) {
            Glide.with(this).load(toUser!!.profileImage).into(img_chat_to_toolbar)
        } else
            img_chat_to_toolbar.setImageResource(R.drawable.ic_person_gray)
    }

    private fun getCurrentUser() {
        val ref = FirebaseDatabase.getInstance().getReference("Users/${mAuth.uid}")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                fromUser = snapshot.getValue(User::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun listenForMessage() {
        val fromID = mAuth.uid
        val toID = toUser!!.uid ?: return

        val ref = FirebaseDatabase.getInstance().getReference("Message/$fromID/$toID")
        ref.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(Message::class.java)
                if (message != null) {
                    if (message.fromID == fromID) {
                        if (fromUser != null)
                            chatAdapter.add(ChatFromItem(message.text, fromUser!!))
                    } else {
                        chatAdapter.add(ChatToItem(message.text, toUser!!))
                    }
                }
                // scroll to latest message
                rcv_message.scrollToPosition(chatAdapter.itemCount - 1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun saveMessageToDB() {
        val strMessage = edt_enter_message.text.toString().trim()
        val fromID = FirebaseAuth.getInstance().uid
        val toID = toUser!!.uid ?: return

        val fromRef = FirebaseDatabase.getInstance().getReference("Message/$fromID/$toID").push()
        val toRef = FirebaseDatabase.getInstance().getReference("Message/$toID/$fromID").push()

        val message = Message(fromRef.key!!, strMessage, System.currentTimeMillis(), fromID!!, toID)
        fromRef.setValue(message).addOnSuccessListener {
            // clear keyboard
            edt_enter_message.text.clear()
            // scroll down to newest message
            rcv_message.scrollToPosition(chatAdapter.itemCount - 1)
        }
        toRef.setValue(message)

        val fromLatestMessageRef = FirebaseDatabase.getInstance()
            .getReference("LatestMessage/$fromID/$toID").setValue(message)

        val toLatestMessageRef = FirebaseDatabase.getInstance()
            .getReference("LatestMessage/$toID/$fromID").setValue(message)

        // send notification
        val roomID = MyCommon.generateChatRoomId(message.fromID, message.toID)
        sendMessageNotification(message, roomID)
    }

    private fun sendMessageNotification(message: Message, roomID: String) {
        val notiData = HashMap<String, String>()
        notiData["mess_title"] = "New Message"
        notiData["mess_content"] = message.text
        notiData["mess_sender"] = message.fromID
        notiData["mess_receiver"] = message.toID
        notiData["mess_roomID"] = roomID
        notiData["notificationType"] = "New Message"

        val sendData = FCMSendData("/topics/${MyCommon.FCM_TOPIC_MESSAGE}", notiData)

        mCompositeDisposable.add(
            ifcmService!!.sendNotification(sendData)
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
        MyCommon.roomSelected = ""
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_send_message -> {
                saveMessageToDB()
            }
            R.id.img_chat_log_back -> {
                onBackPressed()
            }
        }
    }
}


