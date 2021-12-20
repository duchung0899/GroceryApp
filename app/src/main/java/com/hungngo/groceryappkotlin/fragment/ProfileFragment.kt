package com.hungngo.groceryappkotlin.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.activity.MainBuyerActivity
import com.hungngo.groceryappkotlin.activity.UserOrderActivity
import com.hungngo.groceryappkotlin.model.Order
import com.hungngo.groceryappkotlin.model.User
import kotlinx.android.synthetic.main.activity_main_buyer.*
import kotlinx.android.synthetic.main.fragment_profile.*

@SuppressLint("UseSwitchCompatOrMaterialCode")
class ProfileFragment : Fragment(), View.OnClickListener {

    private var user: User? = null

    private var confirmCounter = 0
    private var pendingCounter = 0
    private var ratingCounter = 0

    private lateinit var tvConfirmCounter: TextView
    private lateinit var tvPendingCounter: TextView

    private lateinit var swOrderNotification: Switch
    private lateinit var swMessageNotification: Switch

    private lateinit var sp: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var isEnableOrderNotification = false
    private var isEnableMessageNotification = false

    private val enableMessage = "Notifications are enable"
    private val disableMessage = "Notifications are disable"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("CommitPrefEdits")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // hide toolbar
        val mainActivity = activity as MainBuyerActivity
        mainActivity.rl_buyer.visibility = View.GONE

        // mapping
        tvConfirmCounter = view.findViewById(R.id.tv_counter_confirm)
        tvPendingCounter = view.findViewById(R.id.tv_counter_pending)
        swOrderNotification = view.findViewById(R.id.sw_notification_order)
        swMessageNotification = view.findViewById(R.id.sw_notification_message)

        // set click
        tv_profile_more.setOnClickListener(this)
        img_confirm.setOnClickListener(this)
        img_pending.setOnClickListener(this)
        img_review.setOnClickListener(this)

        // get user's information
        getUserInfo()

        // get counter
        getCounter()

        // init shared preference
        sp = this.requireActivity().getSharedPreferences(MyCommon.SP_KEY, Context.MODE_PRIVATE)
        editor = sp.edit()

        // check last selected option: true/false
        isEnableOrderNotification = sp.getBoolean(MyCommon.SP_ORDER_NOTI_KEY, false)
        isEnableMessageNotification = sp.getBoolean(MyCommon.SP_MESSAGE_NOTI_KEY, false)
        swOrderNotification.isChecked = isEnableOrderNotification
        swMessageNotification.isChecked = isEnableMessageNotification

        // add switch check change listener to enable/disable notification
        swOrderNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // checked, enable notification
                subscribeToTopic(MyCommon.FCM_TOPIC_ORDER, MyCommon.SP_ORDER_NOTI_KEY)
            } else {
                // else, disable
                unSubscribeFromTopic(MyCommon.FCM_TOPIC_ORDER, MyCommon.SP_ORDER_NOTI_KEY)
            }
        }

        swMessageNotification.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // checked, enable notification
                subscribeToTopic(MyCommon.FCM_TOPIC_MESSAGE, MyCommon.SP_MESSAGE_NOTI_KEY)
            } else {
                // else, disable
                unSubscribeFromTopic(MyCommon.FCM_TOPIC_MESSAGE, MyCommon.SP_MESSAGE_NOTI_KEY)
            }
        }
    }

    private fun getCounter() {
        val orderList = mutableListOf<Order?>()
        FirebaseDatabase.getInstance().getReference("Order").child(user!!.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    orderList.clear()
                    for (sn in snapshot.children) {
                        val order = sn.getValue(Order::class.java)
                        orderList.add(order)
                    }
                    setCounter(orderList)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun setCounter(orderList: MutableList<Order?>) {
        orderList.forEach {
            when (it?.orderStatus) {
                MyCommon.CONFIRM -> {
                    confirmCounter++
                }
                MyCommon.PROGRESS -> {
                    pendingCounter++
                }
            }
        }

        showCounter(confirmCounter, tvConfirmCounter)
        showCounter(pendingCounter, tvPendingCounter)
    }

    private fun showCounter(counter: Int, textViewCounter: TextView) {
        if (counter == 0) {
            textViewCounter.visibility = View.GONE
        } else {
            textViewCounter.visibility = View.VISIBLE
            textViewCounter.text = counter.toString()
        }
    }

    private fun getUserInfo() {
        user = arguments?.getSerializable("USER") as User?

        if (user != null) {
            if (user!!.profileImage != null) {
                Glide.with(requireContext()).load(user!!.profileImage).into(img_profile_user)
            } else {
                img_profile_user!!.setImageResource(R.drawable.ic_person_gray)
            }

            tv_profile_name!!.text = user!!.name
            tv_profile_phone!!.text = user!!.phone
        }
    }

    private fun subscribeToTopic(topic: String, spKey: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnSuccessListener {
                // save setting into shared preference
                editor.putBoolean(spKey, true)
                editor.apply()

                Toast.makeText(requireContext(), enableMessage, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun unSubscribeFromTopic(topic: String, spKey: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnSuccessListener {
                // save setting into shared preference
                editor.putBoolean(spKey, false)
                editor.apply()

                Toast.makeText(requireContext(), disableMessage, Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onClick(v: View?) {
        val intent = Intent(requireContext(), UserOrderActivity::class.java)
        when (v!!.id) {
            R.id.tv_profile_more -> {
                intent.putExtra("User_Id", user!!.uid)
                startActivity(intent)
            }
            R.id.img_confirm -> {
                // send position of tab confirm
                intent.putExtra("Position", 0)
                startActivity(intent)
            }
            R.id.img_pending -> {
                // send position of tab pending
                intent.putExtra("Position", 1)
                startActivity(intent)
            }
            R.id.img_review -> {
                // send position of tab review
                intent.putExtra("Position", 3)
                startActivity(intent)
            }
        }
    }

}